package io.jenkins.plugins.lark.notice.sdk.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * SendResult is a utility class that encapsulates the result of a send operation.
 * It contains information about the operation's outcome, including a response code,
 * a message describing the result, and optionally, the request body that was sent.
 *
 * @author xm.z
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendResult {

    /**
     * Response code indicating the outcome of the send operation.
     * A standard value of 0 typically indicates success, whereas -1 indicates failure.
     */
    @JsonAlias({"code", "errcode"})
    private Integer code;

    /**
     * A descriptive message about the outcome of the send operation.
     * This can be an error message or a success confirmation, depending on the operation result.
     */
    @JsonAlias({"msg", "errmsg"})
    private String msg;

    /**
     * The request body data that was sent during the operation.
     * This field is optional and may be null, especially in cases of failure where no request was made.
     */
    private String requestBody;

    /**
     * Creates a failure SendResult instance with a specified message.
     * This is a convenience static method for quickly creating failure results.
     *
     * @param msg the failure message to be associated with the result.
     * @return a SendResult instance representing a failed operation.
     */
    public static SendResult fail(String msg) {
        return new SendResult(-1, msg, null);
    }

    /**
     * Checks if the operation represented by this SendResult instance was successful.
     * Success is defined as having a response code equal to 0.
     *
     * @return true if the operation was successful (code equals 0), false otherwise.
     */
    public boolean isOk() {
        return Objects.nonNull(this.getCode()) && this.getCode() == 0;
    }

}