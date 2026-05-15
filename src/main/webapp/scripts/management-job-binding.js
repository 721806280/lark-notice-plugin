(function () {
    Behaviour.specify('.lark-robot-job-page', 'manage-robot-job-bindings', 0, function (page) {
        if (page.dataset.bindingPageBound === 'true') {
            return;
        }
        page.dataset.bindingPageBound = 'true';

        var state = {
            jobs: [],
            pending: {},
            applying: false
        };

        var applyButton = page.querySelector('[data-apply-bindings]');
        var resetButton = page.querySelector('[data-reset-selection]');
        var selectVisibleButton = page.querySelector('[data-select-visible-jobs]');
        var clearVisibleButton = page.querySelector('[data-clear-visible-jobs]');
        var searchDebounceTimer = null;
        var filterInputs = {
            keyword: page.querySelector('input[name="keyword"]'),
            state: page.querySelector('select[name="state"]')
        };
        if (applyButton && !applyButton.dataset.applyBaseLabel) {
            applyButton.dataset.applyBaseLabel = getButtonLabel(applyButton);
        }

        function scheduleLoadJobs() {
            window.clearTimeout(searchDebounceTimer);
            searchDebounceTimer = window.setTimeout(function () {
                loadJobs(page, state);
            }, 300);
        }

        if (applyButton) {
            applyButton.addEventListener('click', function () {
                if (state.applying) {
                    return;
                }
                applyChanges(page, state);
            });
        }
        if (resetButton) {
            resetButton.addEventListener('click', function () {
                state.pending = {};
                renderJobs(page, state);
                renderPending(page, state);
            });
        }
        if (selectVisibleButton) {
            selectVisibleButton.addEventListener('click', function () {
                updateVisibleSelection(page, state, true);
            });
        }
        if (clearVisibleButton) {
            clearVisibleButton.addEventListener('click', function () {
                updateVisibleSelection(page, state, false);
            });
        }

        Object.keys(filterInputs).forEach(function (key) {
            var input = filterInputs[key];
            if (!input) {
                return;
            }
            input.addEventListener('keydown', function (event) {
                if (event.key === 'Enter') {
                    event.preventDefault();
                    window.clearTimeout(searchDebounceTimer);
                    loadJobs(page, state);
                }
            });
            if (key === 'keyword') {
                input.addEventListener('input', scheduleLoadJobs);
            } else if (key === 'state') {
                input.addEventListener('change', function () {
                    window.clearTimeout(searchDebounceTimer);
                    loadJobs(page, state);
                });
            }
        });

        loadJobs(page, state);
    });

    function loadJobs(page, state) {
        var jobList = page.querySelector('[data-job-list]');
        var params = new URLSearchParams();
        params.append('robotId', page.dataset.robotId || '');
        params.append('keyword', readValue(page, 'input[name="keyword"]'));
        params.append('state', readValue(page, 'select[name="state"]'));

        renderLoadingState(page, jobList);
        LarkNoticeRequest.postForm(page.dataset.loadUrl, params).then(function (payload) {
            var parsed = parseResponse(payload.text);
            var ok = parsed.ok === null ? payload.ok : (payload.ok && parsed.ok);
            if (!ok || !parsed.data) {
                renderJobListError(page, parsed.message || page.dataset.loadRequestFailedMessage || 'Unable to load jobs.');
                return;
            }

            state.jobs = parsed.data.jobs || [];
            renderSummary(page, parsed.data.summary);
            renderJobCount(page, parsed.data);
            renderJobs(page, state);
            renderPending(page, state);
        }).catch(function () {
            renderJobListError(page, page.dataset.loadRequestFailedMessage || 'Unable to load jobs.');
        });
    }

    function applyChanges(page, state) {
        var pendingEntries = pendingEntriesList(state);
        if (pendingEntries.length === 0 || state.applying) {
            return;
        }

        var bindJobs = [];
        var unbindJobs = [];
        pendingEntries.forEach(function (entry) {
            if (entry.desiredSelected) {
                bindJobs.push(entry.jobFullName);
            } else {
                unbindJobs.push(entry.jobFullName);
            }
        });

        var resultContainer = page.querySelector('[data-binding-result]');
        state.applying = true;
        renderPending(page, state);
        LarkNoticeUi.renderAlertMessage(resultContainer, 'lark-robot-job-feedback', page.dataset.applyInProgressLabel || '', true);

        var params = new URLSearchParams();
        params.append('robotId', page.dataset.robotId || '');
        params.append('bindJobFullNames', bindJobs.join('\n'));
        params.append('unbindJobFullNames', unbindJobs.join('\n'));

        LarkNoticeRequest.postForm(page.dataset.applyUrl, params).then(function (payload) {
            var parsed = parseResponse(payload.text);
            var ok = parsed.ok === null ? payload.ok : (payload.ok && parsed.ok);
            var message = parsed.message || page.dataset.applyRequestFailedMessage || 'Unable to apply job changes.';
            LarkNoticeUi.renderAlertMessage(resultContainer, 'lark-robot-job-feedback', message, ok);
            renderApplyResultDetails(page, resultContainer, parsed.data && parsed.data.entries);
            prunePendingEntries(state, parsed.data && parsed.data.entries);
            state.applying = false;
            renderPending(page, state);
            loadJobs(page, state);
        }).catch(function () {
            state.applying = false;
            renderPending(page, state);
            LarkNoticeUi.renderAlertMessage(
                resultContainer,
                'lark-robot-job-feedback',
                page.dataset.applyRequestFailedMessage || 'Unable to apply job changes.',
                false
            );
        });
    }

    function renderSummary(page, summary) {
        if (!summary) {
            return;
        }
        setText(page, '[data-summary-bound]', summary.boundJobCount);
        setText(page, '[data-summary-disabled]', summary.disabledJobCount);
        setText(page, '[data-summary-actionable]', summary.actionableJobCount);
    }

    function renderJobCount(page, data) {
        var target = page.querySelector('[data-list-count]');
        if (!target) {
            return;
        }
        if (!data || !data.jobs) {
            target.textContent = '';
            return;
        }
        if (data.jobs.length === 0) {
            target.textContent = page.dataset.listNoneLabel || 'No jobs match the current filters.';
            return;
        }
        target.textContent = formatMessage(page.dataset.listCountLabel || 'Showing {0} matching jobs.', data.filteredCount);
    }

    function renderJobs(page, state) {
        var container = page.querySelector('[data-job-list]');
        if (!container) {
            return;
        }

        container.textContent = '';
        if (!state.jobs || state.jobs.length === 0) {
            var empty = document.createElement('div');
            empty.className = 'lark-robot-job-list__empty';
            empty.textContent = page.dataset.listNoneLabel || 'No jobs match the current filters.';
            container.appendChild(empty);
            return;
        }

        state.jobs.forEach(function (job) {
            container.appendChild(createJobRow(page, state, job));
        });
        Behaviour.applySubtree(container);
    }

    function updateVisibleSelection(page, state, desiredSelected) {
        visibleActionableJobs(state).forEach(function (job) {
            setPendingSelection(state, job, desiredSelected);
        });
        renderJobs(page, state);
        renderPending(page, state);
    }

    function createJobRow(page, state, job) {
        var row = document.createElement('div');
        row.className = 'lark-job-card';
        row.title = job.actionable ? '' : (job.reason || '');
        applyPendingRowState(row, state, job);
        if (!job.actionable) {
            row.classList.add('is-readonly');
        }

        var checkboxWrap = document.createElement('div');
        checkboxWrap.className = 'lark-robot-job-row__toggle';
        var checkbox = document.createElement('input');
        checkbox.type = 'checkbox';
        checkbox.className = 'lark-job-card__checkbox';
        checkbox.checked = desiredSelected(state, job);
        checkbox.disabled = !job.actionable;
        checkbox.addEventListener('change', function () {
            setPendingSelection(state, job, checkbox.checked);
            applyPendingRowState(row, state, job);
            renderPending(page, state);
        });
        checkboxWrap.appendChild(checkbox);
        row.appendChild(checkboxWrap);

        var main = document.createElement('div');
        main.className = 'lark-job-card__content';

        var identity = document.createElement('div');
        identity.className = 'lark-job-card__identity';

        var title = document.createElement('div');
        title.className = 'lark-job-card__title';
        title.textContent = job.jobDisplayName || job.jobFullName;
        identity.appendChild(title);

        var detail = document.createElement('div');
        detail.className = 'lark-job-card__path';
        detail.textContent = job.jobDescription || job.jobFullName;
        detail.title = job.jobFullName;
        identity.appendChild(detail);
        main.appendChild(identity);

        row.appendChild(main);

        var meta = document.createElement('div');
        meta.className = 'lark-job-card__meta';
        meta.appendChild(createStatusChip(page, job.state));
        var type = document.createElement('span');
        type.className = 'lark-job-card__type';
        type.textContent = job.jobType;
        meta.appendChild(type);

        if (job.reason) {
            var reason = document.createElement('span');
            reason.className = 'lark-job-card__reason';
            reason.textContent = 'i';
            reason.title = job.reason;
            reason.setAttribute('aria-label', job.reason);
            meta.appendChild(reason);
        }

        row.appendChild(meta);
        row.addEventListener('click', function (event) {
            if (!job.actionable || event.target === checkbox || event.target.tagName === 'A') {
                return;
            }
            checkbox.checked = !checkbox.checked;
            checkbox.dispatchEvent(new Event('change', {bubbles: true}));
        });
        return row;
    }

    function setPendingSelection(state, job, selected) {
        if (!job.actionable) {
            return;
        }
        if (selected === job.currentlySelected) {
            delete state.pending[job.jobFullName];
            return;
        }
        state.pending[job.jobFullName] = {
            jobFullName: job.jobFullName,
            jobDisplayName: job.jobDisplayName,
            jobType: job.jobType,
            desiredSelected: selected
        };
    }

    function applyPendingRowState(row, state, job) {
        row.classList.remove('is-pending-bind', 'is-pending-unbind');
        var pending = state.pending[job.jobFullName];
        if (!pending) {
            return;
        }
        row.classList.add(pending.desiredSelected ? 'is-pending-bind' : 'is-pending-unbind');
    }

    function createStatusChip(page, stateKey) {
        var chip = document.createElement('span');
        chip.className = 'lark-job-card__status is-' + stateKey;
        chip.textContent = resolveStatusLabel(page, stateKey);
        return chip;
    }

    function resolveStatusLabel(page, stateKey) {
        switch (stateKey) {
            case 'bound':
                return page.dataset.jobStatusBoundLabel || 'Bound';
            case 'unbound':
                return page.dataset.jobStatusUnboundLabel || 'Unbound';
            case 'disabled':
                return page.dataset.jobStatusDisabledLabel || 'Disabled';
            default:
                return page.dataset.jobStatusReadonlyLabel || 'Read-only';
        }
    }

    function resolveResultStatusLabel(page, status) {
        switch (status) {
            case 'changed':
                return page.dataset.resultChangedLabel || 'Applied';
            case 'failed':
                return page.dataset.resultFailedLabel || 'Failed';
            default:
                return page.dataset.resultSkippedLabel || 'Skipped';
        }
    }

    function renderPending(page, state) {
        var bindContainer = page.querySelector('[data-selection-bind-list]');
        var unbindContainer = page.querySelector('[data-selection-unbind-list]');
        var totalTarget = page.querySelector('[data-selection-total]');
        var bindTarget = page.querySelector('[data-selection-bind]');
        var unbindTarget = page.querySelector('[data-selection-unbind]');
        var applyButton = page.querySelector('[data-apply-bindings]');
        var resetButton = page.querySelector('[data-reset-selection]');
        var selectVisibleButton = page.querySelector('[data-select-visible-jobs]');
        var clearVisibleButton = page.querySelector('[data-clear-visible-jobs]');
        var entries = pendingEntriesList(state);
        var visibleJobs = visibleActionableJobs(state);
        var hasVisibleJobs = visibleJobs.length > 0;
        var allVisibleSelected = hasVisibleJobs && visibleJobs.every(function (job) {
            return desiredSelected(state, job);
        });
        var noVisibleSelected = hasVisibleJobs && visibleJobs.every(function (job) {
            return !desiredSelected(state, job);
        });
        var bindEntries = entries.filter(function (entry) {
            return entry.desiredSelected;
        });
        var unbindEntries = entries.filter(function (entry) {
            return !entry.desiredSelected;
        });

        setNodeCount(totalTarget, entries.length);
        setNodeCount(bindTarget, bindEntries.length);
        setNodeCount(unbindTarget, unbindEntries.length);
        renderPendingGroup(page, bindContainer, bindEntries);
        renderPendingGroup(page, unbindContainer, unbindEntries);

        if (applyButton) {
            applyButton.disabled = state.applying || entries.length === 0;
            setButtonLabel(
                applyButton,
                state.applying
                    ? (page.dataset.applyInProgressLabel || 'Applying...')
                    : formatApplyButtonLabel(applyButton.dataset.applyBaseLabel, entries.length)
            );
        }
        if (resetButton) {
            resetButton.disabled = state.applying || entries.length === 0;
        }
        if (selectVisibleButton) {
            selectVisibleButton.disabled = state.applying || !hasVisibleJobs || allVisibleSelected;
            selectVisibleButton.title = hasVisibleJobs ? '' : (page.dataset.selectionPageEmptyLabel || '');
        }
        if (clearVisibleButton) {
            clearVisibleButton.disabled = state.applying || !hasVisibleJobs || noVisibleSelected;
            clearVisibleButton.title = hasVisibleJobs ? '' : (page.dataset.selectionPageEmptyLabel || '');
        }
    }

    function renderPendingGroup(page, container, entries) {
        if (!container) {
            return;
        }
        container.textContent = '';
        if (entries.length === 0) {
            var empty = document.createElement('div');
            empty.className = 'lark-robot-job-selection__empty';
            empty.textContent = page.dataset.selectionEmptyLabel || 'No pending changes.';
            container.appendChild(empty);
            return;
        }
        entries.forEach(function (entry) {
            var item = document.createElement('div');
            item.className = 'lark-robot-job-selection__item';
            item.appendChild(createPendingItemName(entry));
            container.appendChild(item);
        });
    }

    function createPendingItemName(entry) {
        var wrap = document.createElement('span');
        wrap.className = 'lark-robot-job-selection__job';

        var name = document.createElement('span');
        name.className = 'lark-robot-job-selection__job-name';
        name.textContent = entry.jobDisplayName || entry.jobFullName;
        wrap.appendChild(name);

        if (entry.jobDisplayName && entry.jobDisplayName !== entry.jobFullName) {
            var path = document.createElement('span');
            path.className = 'lark-robot-job-selection__job-path';
            path.textContent = entry.jobFullName;
            wrap.appendChild(path);
        }
        return wrap;
    }

    function prunePendingEntries(state, results) {
        if (!results || !results.length) {
            return;
        }
        results.forEach(function (entry) {
            if (entry && entry.status === 'changed' && entry.jobFullName) {
                delete state.pending[entry.jobFullName];
            }
        });
    }

    function renderApplyResultDetails(page, container, entries) {
        if (!container || !entries || !entries.length) {
            return;
        }

        var details = document.createElement('div');
        details.className = 'lark-robot-job-result-details';

        var title = document.createElement('div');
        title.className = 'lark-robot-job-result-details__title';
        title.textContent = page.dataset.resultDetailsLabel || 'Result Details';
        details.appendChild(title);

        entries.slice(0, 12).forEach(function (entry) {
            var item = document.createElement('div');
            item.className = 'lark-robot-job-result-details__item is-' + (entry.status || 'skipped');

            var status = document.createElement('span');
            status.className = 'lark-robot-job-result-details__status';
            status.textContent = resolveResultStatusLabel(page, entry.status);
            item.appendChild(status);

            var message = document.createElement('span');
            message.className = 'lark-robot-job-result-details__message';
            message.textContent = entry.message || entry.jobFullName || '';
            item.appendChild(message);

            details.appendChild(item);
        });

        if (entries.length > 12) {
            var more = document.createElement('div');
            more.className = 'lark-robot-job-result-details__more';
            more.textContent = '+' + (entries.length - 12);
            details.appendChild(more);
        }

        container.appendChild(details);
    }

    function renderLoadingState(page, container) {
        if (!container) {
            return;
        }
        container.textContent = '';
        var loading = document.createElement('div');
        loading.className = 'lark-robot-job-list__empty';
        loading.textContent = page.dataset.listLoadingLabel || 'Loading jobs...';
        container.appendChild(loading);
        var count = page.querySelector('[data-list-count]');
        if (count) {
            count.textContent = '';
        }
    }

    function renderJobListError(page, message) {
        var container = page.querySelector('[data-job-list]');
        if (!container) {
            return;
        }
        container.textContent = '';
        var alert = LarkNoticeUi.createAlert(message, false);
        container.appendChild(alert);
    }

    function desiredSelected(state, job) {
        if (state.pending[job.jobFullName]) {
            return !!state.pending[job.jobFullName].desiredSelected;
        }
        return !!job.currentlySelected;
    }

    function pendingEntriesList(state) {
        return Object.keys(state.pending).sort().map(function (key) {
            return state.pending[key];
        });
    }

    function visibleActionableJobs(state) {
        return (state.jobs || []).filter(function (job) {
            return job.actionable;
        });
    }

    function readValue(root, selector) {
        var element = root.querySelector(selector);
        return element ? element.value : '';
    }

    function setText(root, selector, value) {
        var element = root.querySelector(selector);
        if (element) {
            element.textContent = value == null ? '--' : String(value);
        }
    }

    function setNodeCount(node, value) {
        if (node) {
            node.textContent = String(value || 0);
        }
    }

    function getButtonLabel(button) {
        var label = button.querySelector('.lark-robot-job-action-button__label');
        return (label || button).textContent.replace(/\s+/g, ' ').trim();
    }

    function setButtonLabel(button, value) {
        var label = button.querySelector('.lark-robot-job-action-button__label');
        if (label) {
            label.textContent = value;
            return;
        }
        button.textContent = value;
    }

    function parseResponse(responseText) {
        var payload = LarkNoticeRequest.parseJsonObject(responseText);
        if (!payload) {
            return {ok: null, message: responseText || '', data: null};
        }
        return {
            ok: typeof payload.ok === 'boolean' ? payload.ok : null,
            message: typeof payload.message === 'string' ? payload.message : '',
            data: payload.data && typeof payload.data === 'object' ? payload.data : null
        };
    }

    function formatMessage(template, value) {
        return String(template || '').replace('{0}', String(value == null ? '' : value));
    }

    function formatApplyButtonLabel(baseLabel, count) {
        if (!baseLabel) {
            baseLabel = 'Apply Changes';
        }
        return count > 0 ? baseLabel + ' (' + count + ')' : baseLabel;
    }
}());
