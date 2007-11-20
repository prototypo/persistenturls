package org.purl.accessor.util;

public class PURLException extends Error {
    private static final long serialVersionUID = 1L;
    private int responseCode;

    /**
     * Create an instance of the PURLException
     * @param message
     * @param responseCode
     */
    public PURLException(String message, int responseCode) {
        super(message);
        this.responseCode = responseCode;
    }

    /**
     * Return the HTTP response code associated with this Exception
     * @return int HTTP response code
     */
    public int getResponseCode() {
        return responseCode;
    }
}
