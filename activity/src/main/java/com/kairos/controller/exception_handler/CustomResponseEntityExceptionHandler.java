package com.kairos.controller.exception_handler;

import com.kairos.commons.config.EnvConfigCommon;
import com.kairos.commons.custom_exception.*;
import com.kairos.commons.service.locale.LocaleService;
import com.kairos.commons.service.mail.SendGridMailService;
import com.kairos.service.exception.ExceptionService;
import com.mindscapehq.raygun4java.core.RaygunClient;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.context.NoSuchMessageException;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

import static com.kairos.constants.ActivityMessagesConstants.INTERNAL_SERVER_ERROR;
import static com.kairos.constants.CommonConstants.LOCAL_PROFILE;

@RestControllerAdvice
@Order(1)
public class CustomResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {


    public static final String EXCEPTION_IN_ACTIVITY_SERVICE = "exception in activity service";

    public CustomResponseEntityExceptionHandler() {
        super();
    }

    @Inject
    private LocaleService localeService;
    @Inject
    private SendGridMailService sendGridMailService;
    @Inject
    private RaygunClient raygunClient;
    @Inject private EnvConfigCommon envConfigCommon;
    @Inject private ExceptionService exceptionService;

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

    @Override
    public ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        logger.error("error in activity service ", ex);
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        List<ObjectError> globalErrors = ex.getBindingResult().getGlobalErrors();
        List<FieldErrorDTO> errors = new ArrayList<FieldErrorDTO>(fieldErrors.size() + globalErrors.size());
        //  String error;
        for (FieldError fieldError : fieldErrors) {
            if(!StringUtils.isEmpty(fieldError.getDefaultMessage())) {
                String message = getErrorMessage(fieldError.getDefaultMessage());
                FieldErrorDTO error = new FieldErrorDTO(fieldError.getField(),message );
                errors.add(error);
            }
        }
        for (ObjectError objectError : globalErrors) {
            if(!StringUtils.isEmpty(objectError.getDefaultMessage())) {
                String message = getErrorMessage(objectError.getDefaultMessage());
                FieldErrorDTO error = new FieldErrorDTO(objectError.getObjectName(), message);
                errors.add(error);
            }
        }

        ResponseEnvelope errorMessage = new ResponseEnvelope();
        errorMessage.setErrors(errors);
        errorMessage.setSuccess(false);

        return new ResponseEntity<Object>(errorMessage, headers, HttpStatus.UNPROCESSABLE_ENTITY);

    }

    private String getErrorMessage(String defaultMessage)  {
        String message;
        try {
            message = convertMessage(defaultMessage);
        }catch (NoSuchMessageException exception){
            message = defaultMessage;
        }
        return message;
    }

    @Override
    public ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        logger.error(EXCEPTION_IN_ACTIVITY_SERVICE, ex);
        ResponseEnvelope errorMessage = new ResponseEnvelope();
        errorMessage.setSuccess(false);
        errorMessage.setMessage(ex.getMessage());
        return new ResponseEntity<>(errorMessage, headers, status);

    }

    @Override
    public ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        logger.error(EXCEPTION_IN_ACTIVITY_SERVICE, ex);
        ResponseEnvelope errorMessage = new ResponseEnvelope();
        errorMessage.setSuccess(false);
        errorMessage.setMessage(ex.getMessage());
        return new ResponseEntity<Object>(errorMessage, headers, status);
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        logger.error(EXCEPTION_IN_ACTIVITY_SERVICE, ex);
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
        logger.error(EXCEPTION_IN_ACTIVITY_SERVICE, ex);
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
        logger.error(EXCEPTION_IN_ACTIVITY_SERVICE, ex);
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
        logger.error(EXCEPTION_IN_ACTIVITY_SERVICE, ex);
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
        logger.error(EXCEPTION_IN_ACTIVITY_SERVICE, ex);
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
        logger.error(EXCEPTION_IN_ACTIVITY_SERVICE, ex);
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
        logger.error(EXCEPTION_IN_ACTIVITY_SERVICE, ex);
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
        logger.error(EXCEPTION_IN_ACTIVITY_SERVICE, ex);
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
        logger.error(EXCEPTION_IN_ACTIVITY_SERVICE, ex);
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
        logger.error(EXCEPTION_IN_ACTIVITY_SERVICE, ex);
        ResponseEnvelope errorMessage = new ResponseEnvelope();
        errorMessage.setSuccess(false);
        errorMessage.setMessage(ex.getMessage());
        return handleExceptionInternal(ex, errorMessage, headers, status, request);
    }


    // API

    // 400

    @ExceptionHandler({DataIntegrityViolationException.class})
    public ResponseEntity<Object> handleBadRequest(final Exception ex, final WebRequest request) {
        logger.error(EXCEPTION_IN_ACTIVITY_SERVICE, ex);
        ResponseEnvelope errorMessage = new ResponseEnvelope();
        errorMessage.setSuccess(false);
        errorMessage.setMessage(ex.getMessage());
        return handleExceptionInternal(ex, errorMessage, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    // 403
    @ExceptionHandler({AccessDeniedException.class})
    public ResponseEntity<Object> handleAccessDeniedException(final Exception ex, final WebRequest request) {
        logger.error(EXCEPTION_IN_ACTIVITY_SERVICE, ex);
        ResponseEnvelope errorMessage = new ResponseEnvelope();
        errorMessage.setSuccess(false);
        errorMessage.setMessage(ex.getMessage());
        return new ResponseEntity<Object>(errorMessage, new HttpHeaders(), HttpStatus.FORBIDDEN);
    }


    // 409

    @ExceptionHandler(DuplicateDataException.class)
    protected ResponseEntity<Object> handleConflict(final DuplicateDataException ex, final WebRequest request) {
        logger.error(EXCEPTION_IN_ACTIVITY_SERVICE, ex);
        String message = convertMessage(ex.getMessage(),ex.getParams());
        ResponseEnvelope errorMessage = new ResponseEnvelope();
        errorMessage.setSuccess(false);
        errorMessage.setMessage(message);
        return handleExceptionInternal(ex, errorMessage, new HttpHeaders(), HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler({DataAccessException.class, InvalidDataAccessApiUsageException.class,InvalidRequestException.class})
    protected ResponseEntity<Object> handleConflict(final RuntimeException ex, final WebRequest request) {
        logger.error(EXCEPTION_IN_ACTIVITY_SERVICE, ex);
        ResponseEnvelope errorMessage = new ResponseEnvelope();
        errorMessage.setSuccess(false);
        errorMessage.setMessage(ex.getMessage());
        return handleExceptionInternal(ex, errorMessage, new HttpHeaders(), HttpStatus.CONFLICT, request);
    }

    //409

    @ExceptionHandler({TimeTypeLinkedException.class})
    protected ResponseEntity<Object> handleTimeTypeHasLink(final TimeTypeLinkedException ex, final WebRequest request) {
        logger.error(EXCEPTION_IN_ACTIVITY_SERVICE, ex);
        String message = convertMessage(ex.getMessage(),ex.getParams());
        ResponseEnvelope errorMessage = new ResponseEnvelope();
        errorMessage.setSuccess(false);
        errorMessage.setMessage(message);
        return handleExceptionInternal(ex, errorMessage, new HttpHeaders(), HttpStatus.CONFLICT, request);
    }

    // 500
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    @ExceptionHandler({NullPointerException.class, IllegalArgumentException.class, IllegalStateException.class, Exception.class, MessagingException.class})
    public ResponseEntity<Object> handleInternal(final Exception ex, final WebRequest request, HttpServletRequest httprequest) {
        logger.error(EXCEPTION_IN_ACTIVITY_SERVICE, ex);
        ResponseEnvelope errorMessage = new ResponseEnvelope();
        errorMessage.setSuccess(false);
        errorMessage.setMessage(convertMessage(INTERNAL_SERVER_ERROR));
        errorMessage.setData(ex.getMessage());
        errorMessage.setPath(httprequest.getRequestURL().toString());
        if(!envConfigCommon.getCurrentProfile().equals(LOCAL_PROFILE) && !(ex instanceof CannotCreateTransactionException) && !(ex instanceof HttpServerErrorException) && !(ex instanceof ClientAbortException) && !(ex instanceof ResourceAccessException)) {
            raygunClient.send(ex);
            sendGridMailService.sendMailToBackendOnException(ex);
        }
        logger.error("exception {}", ex.getCause());
        return handleExceptionInternal(ex, errorMessage, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(value = ActionNotPermittedException.class)
    @ResponseBody
    public ResponseEnvelope actionNotPermittedExceptionHandler(ActionNotPermittedException ex, HttpServletRequest request) {
        logger.error(EXCEPTION_IN_ACTIVITY_SERVICE, ex);
        String message = convertMessage(ex.getMessage(),ex.getParams());
        ResponseEnvelope errorMessage = new ResponseEnvelope();
        errorMessage.setSuccess(false);
        errorMessage.setPath(request.getRequestURL().toString());
        errorMessage.setMessage(message);
        return errorMessage;

    }


    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler({TaskDemandException.class})
    @ResponseBody
    public ResponseEnvelope taskDemandExceptionHandler(TaskDemandException ex, HttpServletRequest request) {
        logger.error(EXCEPTION_IN_ACTIVITY_SERVICE, ex);
        String message = convertMessage(ex.getMessage(),ex.getParams());
        ResponseEnvelope errorMessage = new ResponseEnvelope();
        errorMessage.setSuccess(false);
        errorMessage.setPath(request.getRequestURL().toString());
        errorMessage.setMessage(message);
        return errorMessage;

    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler({DataNotFoundByIdException.class})
    @ResponseBody
    public ResponseEnvelope taskDemandExceptionHandler(DataNotFoundByIdException ex, HttpServletRequest request) {
        logger.error(EXCEPTION_IN_ACTIVITY_SERVICE, ex);
        String message = convertMessage(ex.getMessage(),ex.getParams());
        ResponseEnvelope errorMessage = new ResponseEnvelope();
        errorMessage.setSuccess(false);
        errorMessage.setPath(request.getRequestURL().toString());
        errorMessage.setMessage(message);
        return errorMessage;

    }


    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(value = InvalidClientException.class)
    @ResponseBody
    public ResponseEnvelope clientExceptionHandler(InvalidClientException ex, HttpServletRequest request) {
        String message = convertMessage(ex.getMessage(),ex.getParams());
        logger.error(EXCEPTION_IN_ACTIVITY_SERVICE, ex);
        ResponseEnvelope errorMessage = new ResponseEnvelope();
        errorMessage.setSuccess(false);
        errorMessage.setPath(request.getRequestURL().toString());
        errorMessage.setMessage(message);
        return errorMessage;

    }


    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(value = FlsCredentialException.class)
    @ResponseBody
    public ResponseEnvelope flsCredentialExceptionHandler(FlsCredentialException ex, HttpServletRequest request) {
        String message = convertMessage(ex.getMessage(),ex.getParams());
        logger.error(EXCEPTION_IN_ACTIVITY_SERVICE, ex);
        ResponseEnvelope errorMessage = new ResponseEnvelope();
        errorMessage.setSuccess(false);
        errorMessage.setPath(request.getRequestURL().toString());
        errorMessage.setMessage(message);
        return errorMessage;


    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({DataNotFoundException.class})
    @ResponseBody
    public ResponseEnvelope dataNotFound(DataNotFoundException ex, HttpServletRequest request) {
        String message = convertMessage(ex.getMessage(),ex.getParams());
        logger.error(EXCEPTION_IN_ACTIVITY_SERVICE, ex);
        ResponseEnvelope errorMessage = new ResponseEnvelope();
        errorMessage.setSuccess(false);
        errorMessage.setPath(request.getRequestURL().toString());
        errorMessage.setMessage(message);
        return errorMessage;

    }

    @ResponseStatus(HttpStatus.NOT_MODIFIED)
    @ExceptionHandler({DataNotModifiedException.class})
    @ResponseBody
    public ResponseEnvelope dataNotModified(DataNotModifiedException ex, HttpServletRequest request) {
        String message = convertMessage(ex.getMessage(),ex.getParams());
        logger.error(EXCEPTION_IN_ACTIVITY_SERVICE, ex);
        ResponseEnvelope errorMessage = new ResponseEnvelope();
        errorMessage.setSuccess(false);
        errorMessage.setPath(request.getRequestURL().toString());
        errorMessage.setMessage(message);
        return errorMessage;

    }



}
