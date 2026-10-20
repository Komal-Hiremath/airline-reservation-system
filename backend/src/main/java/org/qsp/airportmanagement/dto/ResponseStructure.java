
package org.qsp.airportmanagement.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class ResponseStructure<T> {

    private boolean success;
    private int statusCode;
    private String message;
    private T data;

    public static <T> ResponseStructure<T> success(int statusCode, String message, T data) {
        return new ResponseStructure<>(true, statusCode, message, data);
    }

    public static <T> ResponseStructure<T> failure(int statusCode, String message) {
        return new ResponseStructure<>(false, statusCode, message, null);
    }
}
