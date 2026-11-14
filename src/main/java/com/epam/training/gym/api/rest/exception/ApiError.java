package com.epam.training.gym.api.rest.exception;

/**
 * Body of every error response.
 *
 * @param status  HTTP status code, for example 404
 * @param error   HTTP status reason, for example "Not Found"
 * @param message what went wrong, safe to show to the client
 */
public record ApiError(int status, String error, String message) {
}
