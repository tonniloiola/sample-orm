package com.example.demo.exceptions;


public class ResultsetMappingException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ResultsetMappingException() {
        super();
    }

    public ResultsetMappingException(String message) {
        super("Error while mapping ResultSet: "+message);
    }
}
