package com.kairos.controller.exception_handler;

import com.kairos.commons.config.EnvConfigCommon;
import com.kairos.commons.custom_exception.*;
import com.kairos.commons.service.locale.LocaleService;
import com.kairos.commons.service.mail.SendGridMailService;
import com.kairos.custom_exception.UnitNotFoundException;
import com.kairos.wrapper.ResponseEnvelope;
import com.mindscapehq.raygun4java.core.RaygunClient;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.kairos.constants.CommonConstants.LOCAL_PROFILE;
import static com.kairos.constants.UserMessagesConstants.INTERNAL_SERVER_ERROR;

//import com.mindscapehq.raygun4java.core.RaygunClient;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CustomResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {


    public static final String ERROR_IN_USER_SERVICE = "error in user service";
    @Autowired
    private LocaleService localeService;
    @Inject
    private SendGridMailService sendGridMailService;

    @Inject
    private SendGridMailService mailService;
    @Inject private EnvConfigCommon envConfigCommon;
    @Inject private RaygunClient raygunClient;

    private String convertMessage(String message, Object... params) {
        for (int i = 0; i < params.length; i++) {
            try {
                params[i] = localeService.getMessage(params[i].toString());
            } catch (Exception e) {
                // intentionally left empty
            }
        }
        return localeService.getMessage(message, params);
    }
    public CustomResponseEntityExceptionHandler() {
        super();
    }

    @Override
    public ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        logger.error(ERROR_IN_USER_SERVICE + " ", ex);
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        List<ObjectError> globalErrors = ex.getBindingResult().getGlobalErrors();
        List<FieldErrorDTO> errors = new ArrayList<FieldErrorDTO>(fieldErrors.size() + globalErrors.size());
        //  String error;
        for (FieldError fieldError : fieldErrors) {
            if(!StringUtils.isEmpty(fieldError.getDefaultMessage())) {
                FieldErrorDTO error = new FieldErrorDTO(fieldError.getField(), convertMessage(fieldError.getDefaultMessage()));
                errors.add(error);
            }
        }
        for (ObjectError objectError : globalErrors) {
            if(!StringUtils.isEmpty(objectError.getDefaultMessage())) {
                FieldErrorDTO error = new FieldErrorDTO(objectError.getObjectName(), convertMessage(objectError.getDefaultMessage()));
                errors.add(error);
            }
        }

        ResponseEnvelope errorMessage = new ResponseEnvelope();
        errorMessage.setErrors(errors);
        errorMessage.setSuccess(false);
        return new ResponseEntity<Object>(errorMessage, headers, HttpStatus.UNPROCESSABLE_ENTITY);

    }


    @Override
    public ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        logger.error(ERROR_IN_USER_SERVICE + " ", ex);
        String unsupported = "Unsupported content type: " + ex.getContentType();
        String supported = "Supported content types: " + MediaType.toString(ex.getSupportedMediaTypes());
        ResponseEnvelope errorMessage = new ResponseEnvelope();
        errorMessage.setSuccess(false);
        errorMessage.setMessage(ex.getMessage());
        return new ResponseEntity<Object>(errorMessage, headers, status);

    }

    @Override
    public ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        logger.error(ERROR_IN_USER_SERVICE + " ", ex);
        ResponseEnvelope errorMessage = new ResponseEnvelope();
        errorMessage.setSuccess(false);
        errorMessage.setMessage(ex.getMessage());
        return new ResponseEntity<Object>(errorMessage, headers, status);
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        logger.error(ERROR_IN_USER_SERVICE + " ", ex);
        ResponseEnvelope errorMessage = new ResponseEnvelope();
        errorMessage.setSuccess(false);
        errorMessage.setPath(ex.getRequestURL());
        errorMessage.setMessage(ex.getMessage());
        return new ResponseEntity<Object>(errorMessage, HttpStatus.NOT_FOUND);
    }


    /**
     * Customize the response for HttpRequestMethodNotSupportedException.
     * <p>This method logs a warning, sets the "Allow" header, and delegates to
     * {@link #handleExceptionInternal}.
     *
     * @param ex      the exception
     * @param headers the headers to be written to the response
     * @param status  the selected response status
     * @param request the current request
     * @return a {@code ResponseEntity} instance
     */
    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex,
                                                                         HttpHeaders headers, HttpStatus status, WebRequest request) {
        logger.error(ERROR_IN_USER_SERVICE + " ", ex);
        pageNotFoundLogger.warn(ex.getMessage());

        Set<HttpMethod> supportedMethods = ex.getSupportedHttpMethods();
        if (!supportedMethods.isEmpty()) {
            headers.setAllow(supportedMethods);
        }
        ResponseEnvelope errorMessage = new ResponseEnvelope();
        errorMessage.setSuccess(false);
        errorMessage.setMessage(ex.getMessage());
        return handleExceptionInternal(ex, errorMessage, headers, status, request);
    }

    /**
     * Customize the response for HttpMediaTypeNotAcceptableException.
     * <p>This method delegates to {@link #handleExceptionInternal}.
     *
     * @param ex      the exception
     * @param headers the headers to be written to the response
     * @param status  the selected response status
     * @param request the current request
     * @return a {@code ResponseEntity} instance
     */
    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException ex,
                                                                      HttpHeaders headers, HttpStatus status, WebRequest request) {
        logger.error(ERROR_IN_USER_SERVICE + " ", ex);
        ResponseEnvelope errorMessage = new ResponseEnvelope();
        errorMessage.setSuccess(false);
        errorMessage.setMessage(ex.getMessage());
        return handleExceptionInternal(ex, errorMessage, headers, status, request);
    }

    /**
     * Customize the response for MissingPathVariableException.
     * <p>This method delegates to {@link #handleExceptionInternal}.
     *
     * @param ex      the exception
     * @param headers the headers to be written to the response
     * @param status  the selected response status
     * @param request the current request
     * @return a {@code ResponseEntity} instance
     * @since 4.2
     */
    @Override
    protected ResponseEntity<Object> handleMissingPathVariable(MissingPathVariableException ex,
                                                               HttpHeaders headers, HttpStatus status, WebRequest request) {
        logger.error(ERROR_IN_USER_SERVICE + " ", ex);
        ResponseEnvelope errorMessage = new ResponseEnvelope();
        errorMessage.setSuccess(false);
        errorMessage.setMessage(ex.getMessage());
        return handleExceptionInternal(ex, errorMessage, headers, status, request);
    }

    /**
     * Customize the response for MissingServletRequestParameterException.
     * <p>This method delegates to {@link #handleExceptionInternal}.
     *
     * @param ex      the exception
     * @param headers the headers to be written to the response
     * @param status  the selected response status
     * @param request the current request
     * @return a {@code ResponseEntity} instance
     */
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex,
                                                                          HttpHeaders headers, HttpStatus status, WebRequest request) {
        logger.error(ERROR_IN_USER_SERVICE + " ", ex);
        ResponseEnvelope errorMessage = new ResponseEnvelope();
        errorMessage.setSuccess(false);
        errorMessage.setMessage(ex.getMessage());
        return handleExceptionInternal(ex, errorMessage, headers, status, request);
    }

    /**
     * Customize the response for ServletRequestBindingException.
     * <p>This method delegates to {@link #handleExceptionInternal}.
     *
     * @param ex      the exception
     * @param headers the headers to be written to the response
     * @param status  the selected response status
     * @param request the current request
     * @return a {@code ResponseEntity} instance
     */
    @Override
    protected ResponseEntity<Object> handleServletRequestBindingException(ServletRequestBindingException ex,
                                                                          HttpHeaders headers, HttpStatus status, WebRequest request) {
        logger.error(ERROR_IN_USER_SERVICE + " ", ex);
        ResponseEnvelope errorMessage = new ResponseEnvelope();
        errorMessage.setSuccess(false);
        errorMessage.setMessage(ex.getMessage());
        return handleExceptionInternal(ex, errorMessage, headers, status, request);
    }

    /**
     * Customize the response for ConversionNotSupportedException.
     * <p>This method delegates to {@link #handleExceptionInternal}.
     *
     * @param ex      the exception
     * @param headers the headers to be written to the response
     * @param status  the selected response status
     * @param request the current request
     * @return a {@code ResponseEntity} instance
     */

    @Override
    protected ResponseEntity<Object> handleConversionNotSupported(ConversionNotSupportedException ex,
                                                                  HttpHeaders headers, HttpStatus status, WebRequest request) {
        logger.error(ERROR_IN_USER_SERVICE + " ", ex);
        ResponseEnvelope errorMessage = new ResponseEnvelope();
        errorMessage.setSuccess(false);
        errorMessage.setMessage(ex.getMessage());
        return handleExceptionInternal(ex, errorMessage, headers, status, request);
    }


    /**
     * Customize the response for HttpMessageNotWritableException.
     * <p>This method delegates to {@link #handleExceptionInternal}.
     *
     * @param ex      the exception
     * @param headers the headers to be written to the response
     * @param status  the selected response status
     * @param request the current request
     * @return a {@code ResponseEntity} instance
     */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotWritable(HttpMessageNotWritableException ex,
                                                                  HttpHeaders headers, HttpStatus status, WebRequest request) {
        logger.error(ERROR_IN_USER_SERVICE + " ", ex);
        ResponseEnvelope errorMessage = new ResponseEnvelope();
        errorMessage.setSuccess(false);
        errorMessage.setMessage(ex.getMessage());
        return handleExceptionInternal(ex, errorMessage, headers, status, request);
    }

    /**
     * Customize the response for MissingServletRequestPartException.
     * <p>This method delegates to {@link #handleExceptionInternal}.
     *
     * @param ex      the exception
     * @param headers the headers to be written to the response
     * @param status  the selected response status
     * @param request the current request
     * @return a {@code ResponseEntity} instance
     */
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestPart(MissingServletRequestPartException ex,
                                                                     HttpHeaders headers, HttpStatus status, WebRequest request) {
        logger.error(ERROR_IN_USER_SERVICE + " ", ex);
        ResponseEnvelope errorMessage = new ResponseEnvelope();
        errorMessage.setSuccess(false);
        errorMessage.setMessage(ex.getMessage());
        return handleExceptionInternal(ex, errorMessage, headers, status, request);
    }

    /**
     * Customize the response for BindException.
     * <p>This method delegates to {@link #handleExceptionInternal}.
     *
     * @param ex      the exception
     * @param headers the headers to be written to the response
     * @param status  the selected response status
     * @param request the current request
     * @return a {@code ResponseEntity} instance
     */
    @Override
    protected ResponseEntity<Object> handleBindException(BindException ex, HttpHeaders headers,
                                                         HttpStatus status, WebRequest request) {
        logger.error(ERROR_IN_USER_SERVICE + " ", ex);
        ResponseEnvelope errorMessage = new ResponseEnvelope();
        errorMessage.setSuccess(false);
        errorMessage.setMessage(ex.getMessage());
        return handleExceptionInternal(ex, errorMessage, headers, status, request);
    }


    // API

    // 400

    @ExceptionHandler({DataIntegrityViolationException.class})
    public ResponseEntity<Object> handleBadRequest(final Exception ex, final WebRequest request) {
        logger.error(ERROR_IN_USER_SERVICE + " ", ex);
        ResponseEnvelope errorMessage = new ResponseEnvelope();
        errorMessage.setSuccess(false);
        errorMessage.setMessage(ex.getMessage());
        return handleExceptionInternal(ex, errorMessage, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    // 403
    @ExceptionHandler({AccessDeniedException.class})
    public ResponseEntity<Object> handleAccessDeniedException(final Exception ex, final WebRequest request) {
        logger.error(ERROR_IN_USER_SERVICE + " ", ex);
        ResponseEnvelope errorMessage = new ResponseEnvelope();
        errorMessage.setSuccess(false);
        errorMessage.setMessage(ex.getMessage());
        return new ResponseEntity<Object>(errorMessage, new HttpHeaders(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler({InvalidRequestException.class})
    public ResponseEntity<Object> handleAccessDeniedException(final InvalidRequestException ex, final WebRequest request) {
        logger.error(ERROR_IN_USER_SERVICE + " ", ex);
        String message = convertMessage(ex.getMessage(),ex.getParams());
        ResponseEnvelope errorMessage = new ResponseEnvelope();
        errorMessage.setSuccess(false);
        errorMessage.setMessage(ex.getMessage());
        return new ResponseEntity<Object>(errorMessage, new HttpHeaders(), HttpStatus.FORBIDDEN);
    }


    @ExceptionHandler({DuplicateDataException.class})
    protected ResponseEntity<Object> handleConflict(final DuplicateDataException ex, final WebRequest request) {
        logger.error(ERROR_IN_USER_SERVICE + " ", ex);
        String message = convertMessage(ex.getMessage(),ex.getParams());
        ResponseEnvelope errorMessage = new ResponseEnvelope();
        errorMessage.setSuccess(false);
        errorMessage.setMessage(ex.getMessage());
        return handleExceptionInternal(ex, errorMessage, new HttpHeaders(), HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler({DataAccessException.class, InvalidDataAccessApiUsageException.class})
    protected ResponseEntity<Object> handleConflict(final RuntimeException ex, final WebRequest request) {
        logger.error(ERROR_IN_USER_SERVICE + " ", ex);
        ResponseEnvelope errorMessage = new ResponseEnvelope();
        errorMessage.setSuccess(false);
        errorMessage.setMessage(ex.getMessage());
        return handleExceptionInternal(ex, errorMessage, new HttpHeaders(), HttpStatus.CONFLICT, request);
    }

    // 500
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    @ExceptionHandler({NullPointerException.class, IllegalArgumentException.class, IllegalStateException.class, Exception.class, MessagingException.class})
    public ResponseEnvelope handleInternal(final Exception ex, final WebRequest request) {
        logger.error(ERROR_IN_USER_SERVICE + " ", ex);
        ResponseEnvelope errorMessage = new ResponseEnvelope();
        errorMessage.setSuccess(false);
        errorMessage.setMessage(convertMessage(INTERNAL_SERVER_ERROR));

        if(!envConfigCommon.getCurrentProfile().equals(LOCAL_PROFILE) && !(ex instanceof CannotCreateTransactionException) && !(ex instanceof HttpServerErrorException) && !(ex instanceof ClientAbortException) && !(ex instanceof ResourceAccessException)) {
            raygunClient.send(ex);
            mailService.sendMailToBackendOnException(ex);
        }
        return errorMessage;//handleExceptionInternal(ex, errorMessage, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }


    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({
            ZipCodeNotFound.class})
    @ResponseBody
    public ResponseEnvelope handleNotFound(ZipCodeNotFound ex, HttpServletRequest request) {
        logger.error(ERROR_IN_USER_SERVICE + " ", ex);
        String message = convertMessage(ex.getMessage(),ex.getParams());
        return getResponseEnvelope(request, message);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({
            CitizenNotFoundException.class})
    @ResponseBody
    public ResponseEnvelope handleNotFound(CitizenNotFoundException ex, HttpServletRequest request) {
        logger.error(ERROR_IN_USER_SERVICE + " ", ex);
        String message = convertMessage(ex.getMessage(),ex.getParams());
        return getResponseEnvelope(request, message);
    }


    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({AddressNotVerifiedByTomTom.class})
    @ResponseBody
    public ResponseEnvelope handleNotFound(AddressNotVerifiedByTomTom ex, HttpServletRequest request) {
        logger.error(ERROR_IN_USER_SERVICE + " ", ex);
        String message = convertMessage(ex.getMessage(),ex.getParams());
        return getResponseEnvelope(request, message);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({DataNotFoundByIdException.class})
    @ResponseBody
    public ResponseEnvelope handleNotFound(DataNotFoundByIdException ex, HttpServletRequest request) {
        logger.error(ERROR_IN_USER_SERVICE + " ", ex);
        String message = convertMessage(ex.getMessage(),ex.getParams());
        return getResponseEnvelope(request, message);
    }


    private ResponseEnvelope getResponseEnvelope(HttpServletRequest request, String message) {
        ResponseEnvelope errorMessage = new ResponseEnvelope();
        errorMessage.setSuccess(false);
        errorMessage.setPath(request.getRequestURL().toString());
        errorMessage.setMessage(message);
        return errorMessage;
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(value = ActionNotPermittedException.class)
    @ResponseBody
    public ResponseEnvelope actionNotPermittedExceptionHandler(ActionNotPermittedException ex, HttpServletRequest request) {
        logger.error(ERROR_IN_USER_SERVICE + " ", ex);
        String message = convertMessage(ex.getMessage(),ex.getParams());
        return getResponseEnvelope(request, message);
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(value = TaskDemandException.class)
    @ResponseBody
    public ResponseEnvelope taskDemandExceptionHandler(TaskDemandException ex, HttpServletRequest request) {
        logger.error(ERROR_IN_USER_SERVICE + " ", ex);
        String message = convertMessage(ex.getMessage(),ex.getParams());
        return getResponseEnvelope(request, message);
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(value = DataNotMatchedException.class)
    @ResponseBody
    public ResponseEnvelope dataNotMatchedExceptionHandler(DataNotMatchedException ex, HttpServletRequest request) {
        logger.error(ERROR_IN_USER_SERVICE + " ", ex);
        String message = convertMessage(ex.getMessage(),ex.getParams());
        return getResponseEnvelope(request, message);
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(value = FlsCredentialException.class)
    @ResponseBody
    public ResponseEnvelope flsCredentialExceptionHandler(FlsCredentialException ex, HttpServletRequest request) {
        logger.error(ERROR_IN_USER_SERVICE + " ", ex);
        String message = convertMessage(ex.getMessage(),ex.getParams());
        return getResponseEnvelope(request, message);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = UnitNotFoundException.class)
    @ResponseBody
    public ResponseEnvelope unitNotFoundExceptionHandler(UnitNotFoundException ex, HttpServletRequest request) {
        logger.error("Invalid unit id ", ex);
        String message = convertMessage(ex.getMessage(),ex.getParams());
        return getResponseEnvelope(request, message);
    }

}
