package monitor;

import com.mason.project.monitor.ErrorCode;
import com.mason.project.monitor.MetricManager;
import com.mason.project.monitor.RequestMetric;

import java.rmi.RemoteException;

public class Sample {

    private HttpService httpClient = new HttpService();

    /**
     * not must. for sample only.
     */
    public static class BizErrorException extends RuntimeException {
    }

    /**
     * not must. for sample only.
     */
    public static class RemoteServiceException extends RuntimeException {
        ErrorCode errorCode;

        public RemoteServiceException(ErrorCode errorCode) {
            this.errorCode = errorCode;
        }
    }

    /**
     * not must. for sample only.
     */
    public class HttpService {
        Response doRequest(Request request) throws RemoteException {
            return new Response(ErrorCode.SUCCESS);
        }
    }

    /**
     * not must. for sample only.
     */
    public static class Request {
        String method;

        public Request(String method) {
            this.method = method;
        }
    }

    /**
     * not must. for sample only.
     */
    public static class Response {
        private ErrorCode errorCode;

        public Response(ErrorCode errorCode) {
            this.errorCode = errorCode;
        }
    }

    public Response handleRequest(Request request) {
        RequestMetric inRequestMetric = MetricManager.getInstance().getOrRegisterInReq(request.method);
        RequestMetric.Context context = inRequestMetric.time();

        ErrorCode errorCode = ErrorCode.SUCCESS;
        try {
            // do some biz logic
            // call other service
            Request callRequest = new Request("callOther");
            Response response = callOtherModule(callRequest);
            return response;
        } catch (BizErrorException e) {
            errorCode = ErrorCode.COMMON_ILLEGAL_ARG_ERROR;
            return new Response(errorCode);
        } catch (RemoteServiceException e) {
            errorCode = e.errorCode;
            return new Response(errorCode);
        } catch (Exception e) {
            errorCode = ErrorCode.INTERNAL_ERROR;
            return new Response(errorCode);
        } finally {
            // notice that this errorCode is module errorcode. not error code from sub module.
            context.stop(errorCode);
        }
    }

    /**
     * return response or throw RemoteServiceException
     */
    public Response callOtherModule(Request request) throws RemoteServiceException {
        // this out request
        RequestMetric outRequestMetric = MetricManager.getInstance().getOrRegisterOutReq(request.method);
        RequestMetric.Context context = outRequestMetric.time();
        ErrorCode errorCode = ErrorCode.SUCCESS;
        try {
            return httpClient.doRequest(request);
        } catch (RemoteException e) {
            errorCode = ErrorCode.INTERNAL_SERVER_TIMEOUT_ERROR; // or the exception
            throw new RemoteServiceException(errorCode);
        } finally {
            // notice that this errorCode is module errorcode. not error code from sub module.
            context.stop(errorCode);
        }
    }
}
