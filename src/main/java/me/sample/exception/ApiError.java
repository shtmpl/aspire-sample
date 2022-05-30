package me.sample.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiError { // will be changed to jsend in future

	private String field;
	private String value;
	private String message;

	public ApiError(String message) {
		this.message = message;
	}
}
