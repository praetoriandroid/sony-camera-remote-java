package com.praetoriandroid.cameraremote.tool;

import com.praetoriandroid.cameraremote.DeviceDescription;
import com.praetoriandroid.cameraremote.GetExposureModeRequest;
import com.praetoriandroid.cameraremote.HttpClient;
import com.praetoriandroid.cameraremote.Logger;
import com.praetoriandroid.cameraremote.RpcClient;
import com.praetoriandroid.cameraremote.SsdpClient;
import com.praetoriandroid.cameraremote.rpc.ActTakePictureRequest;
import com.praetoriandroid.cameraremote.rpc.ActTakePictureResponse;
import com.praetoriandroid.cameraremote.rpc.AwaitTakePictureRequest;
import com.praetoriandroid.cameraremote.rpc.BaseRequest;
import com.praetoriandroid.cameraremote.rpc.BaseResponse;
import com.praetoriandroid.cameraremote.rpc.EventEntity;
import com.praetoriandroid.cameraremote.rpc.GetAvailableApiListRequest;
import com.praetoriandroid.cameraremote.rpc.GetAvailableApiListResponse;
import com.praetoriandroid.cameraremote.rpc.GetEventRequest;
import com.praetoriandroid.cameraremote.rpc.GetEventResponse;
import com.praetoriandroid.cameraremote.rpc.GetMethodTypesRequest;
import com.praetoriandroid.cameraremote.rpc.GetMethodTypesResponse;
import com.praetoriandroid.cameraremote.rpc.GetVersionsRequest;
import com.praetoriandroid.cameraremote.rpc.StartLiveviewRequest;
import com.praetoriandroid.cameraremote.rpc.StartLiveviewResponse;
import com.praetoriandroid.cameraremote.rpc.StopLiveviewRequest;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RemoteControl implements Logger {

    private static final String KEY_CAMERA_SERVICE_URL = "cameraServiceUrl";

    private static final boolean DEBUG = true;

    private boolean interactive = false;

    private RpcClient rpcClient;

    private HttpClient httpClient = new HttpClient();

    private String lastFetchUrl;

    private RemoteControl()
            throws IOException, DeviceDescription.ServiceNotSupportedException, SsdpClient.SsdpException {
        String cacheDir = System.getenv("HOME");
        if (cacheDir == null) {
            cacheDir = "/tmp";
        }
        cacheDir += "/." + RemoteControl.class.getSimpleName().toLowerCase();

        Cache config = new Cache(cacheDir, "config");
        rpcClient = createClient(config);
        rpcClient.setLogger(this);
    }

    public static void main(String[] args) {
        try {
            RemoteControl remoteControl = new RemoteControl();

            if (args.length > 0) {
                try {
                    Command command = remoteControl.parseCommand(args[0]);
                    command.process(remoteControl, Arrays.copyOfRange(args, 1, args.length));
                } catch (IllegalArgumentException e) {
                    System.exit(1);
                } catch (HttpClient.BadHttpResponseException e) {
                    System.err.println(e.toString());
                    System.exit(1);
                }
            } else {
                remoteControl.goInteractive(remoteControl);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (SsdpClient.SsdpException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (DeviceDescription.ServiceNotSupportedException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void debug(Object data) {
        if (DEBUG) {
            System.out.println(data);
        }
    }

    public void debug(String format, Object... args) {
        System.out.printf(format + '\n', args);
    }

    public void info(Object data) {
        System.out.println(data);
    }

    public void error(Object object) {
        System.err.println(object);
    }

    private Command parseCommand(String commandName) {
        try {
            return Command.valueOf(commandName);
        } catch (IllegalArgumentException e) {
            System.err.println("Unknown command: " + commandName
                    + ". Use '" + Command.help + "' to get list of available commands.");
            throw e;
        }
    }

    private void goInteractive(RemoteControl remoteControl) throws IOException {
        interactive = true;
        try {
            while (true) {
                try {
                    CommandInfo commandInfo = getCommand();
                    if (commandInfo == null) {
                        continue;
                    } else if (commandInfo.command == Command.exit) {
                        break;
                    }
                    commandInfo.command.process(remoteControl, commandInfo.args);
                } catch (IllegalArgumentException ignored) {
                } catch (HttpClient.BadHttpResponseException e) {
                    System.err.println(e.toString());
                }
            }
        } finally {
            rpcClient.sayGoodbye();
        }
    }

    private RpcClient createClient(Cache config)
            throws IOException, DeviceDescription.ServiceNotSupportedException, SsdpClient.SsdpException {
        String cameraServiceUrl = config.get(KEY_CAMERA_SERVICE_URL);
        RpcClient rpcClient;
        boolean discovered = false;
        if (cameraServiceUrl == null) {
            cameraServiceUrl = discoverCameraServiceUrl(config);
            discovered = true;
        }

        rpcClient = new RpcClient(cameraServiceUrl);
        try {
            rpcClient.sayHello();

            List<String> supportedVersions = rpcClient.send(new GetVersionsRequest()).getSupportedVersions();
            debug("Supported versions: %s", supportedVersions);
            if (!supportedVersions.contains("1.0")) {
                System.err.println("API version 1.0 is not supported!");
                System.exit(1);
            }
        } catch (IOException e) {
            if (discovered) {
                throw e;
            } else {
                cameraServiceUrl = discoverCameraServiceUrl(config);
                rpcClient = new RpcClient(cameraServiceUrl);
                rpcClient.sayHello();
            }
        } catch (HttpClient.BadHttpResponseException e) {
            throw new IOException(e);
        }
        return rpcClient;
    }

    private String discoverCameraServiceUrl(Cache config) throws SsdpClient.SsdpException, IOException, DeviceDescription.ServiceNotSupportedException {
        SsdpClient ssdpClient = new SsdpClient();
        String deviceDescriptionUrl = ssdpClient.getDeviceDescriptionUrl();
        DeviceDescription description = new DeviceDescription(deviceDescriptionUrl);
        debug(description);
        String cameraServiceUrl = description.getServiceUrl(DeviceDescription.CAMERA);
        config.put(KEY_CAMERA_SERVICE_URL, cameraServiceUrl);
        return cameraServiceUrl;
    }

    private CommandInfo getCommand() throws IllegalArgumentException {
        try {
            byte[] buffer = new byte[256];
            int length = System.in.read(buffer);
            if (length == -1) {
                return null;
            }
            String commandText = new String(buffer, 0, length, "UTF-8").trim();
            if (commandText.isEmpty()) {
                return null;
            }
            String[] parts = commandText.split(" +");
            try {
                return new CommandInfo(parseCommand(parts[0]), Arrays.copyOfRange(parts, 1, parts.length));
            } catch (IllegalArgumentException e) {
                return null;
            }
        } catch (IOException e) {
            return null;
        }
    }

    private void setLastFetchUrl(String url) {
        lastFetchUrl = url;
    }

    private String getLastFetchUrl() {
        return lastFetchUrl;
    }

    private static class CommandInfo {
        private Command command;
        private String[] args;

        private CommandInfo(Command command, String[] args) {
            this.command = command;
            this.args = args;
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    private static enum Command {
        help {
            @Override
            void process(final RemoteControl remoteControl, String... args)
                    throws IOException, HttpClient.BadHttpResponseException {
                help.printInfo(remoteControl);
                hello.printInfo(remoteControl);
                goodbye.printInfo(remoteControl);
                fetch.printInfo(remoteControl);
                remoteControl.sendCommand(new GetAvailableApiListRequest(), new SuccessfulResponseHandler<GetAvailableApiListResponse>() {
                    @Override
                    public void onSuccess(GetAvailableApiListResponse response) {
                        for (String api : response.getApiList()) {
                            try {
                                Command command = Command.valueOf(api);
                                command.printInfo(remoteControl);
                            } catch (IllegalArgumentException ignored) {
                            }
                        }
                    }
                });
                exit.printInfo(remoteControl);
            }

            @Override
            String getInfo() {
                return "get list af the available commands (it may varies in different camera modes)";
            }
        },
        hello {
            @Override
            void process(RemoteControl remoteControl, String... args) throws IOException {
                remoteControl.rpcClient.sayHello();
            }

            @Override
            String getInfo() {
                return "on some cameras prepares it for execution of the most other commands";
            }
        },
        goodbye {
            @Override
            void process(RemoteControl remoteControl, String... args) throws IOException {
                remoteControl.rpcClient.sayGoodbye();
            }

            @Override
            String getInfo() {
                return "deinitialize what was initialized by the " + hello + " command";
            }
        },
        fetch {
            @Override
            void process(RemoteControl remoteControl, String... args)
                    throws IOException, HttpClient.BadHttpResponseException {
                String url = remoteControl.getLastFetchUrl();
                if (url != null) {
                    String output = url.replaceFirst(".*/", "");
                    remoteControl.httpClient.fetchFile(url, output);
                    remoteControl.info("Picture saved to " + output);
                } else {
                    System.err.println("Url is unavailable. You need to call appropriate command first. For example "
                            + actTakePicture);
                }
            }

            @Override
            String getInfo() {
                return "fetch last picture, captured with " + actTakePicture + " call or similar.";
            }
        },
        exit {
            @Override
            void process(RemoteControl remoteControl, String... args) throws IOException {
                System.exit(0);
            }

            @Override
            String getInfo() {
                return "stop processing commands and say goodbye to the device";
            }
        },
        actTakePicture {
            @Override
            void process(final RemoteControl remoteControl, String... args)
                    throws IOException, HttpClient.BadHttpResponseException {
                remoteControl.sendCommand(new ActTakePictureRequest(), new SuccessfulResponseHandler<ActTakePictureResponse>() {
                    @Override
                    public void onSuccess(ActTakePictureResponse response) {
                        for (String url : response.getUrls()) {
                            remoteControl.info(url);
                        }
                        if (response.getUrls().length > 0) {
                            remoteControl.setLastFetchUrl(response.getUrls()[0]);
                        }
                    }
                });
            }
        },
        awaitTakePicture {
            @Override
            void process(final RemoteControl remoteControl, String... args)
                    throws IOException, HttpClient.BadHttpResponseException {
                remoteControl.sendCommand(new AwaitTakePictureRequest(),
                        new SuccessfulResponseHandler<ActTakePictureResponse>() {
                            @Override
                            public void onSuccess(ActTakePictureResponse response) {
                                for (String url : response.getUrls()) {
                                    remoteControl.info(url);
                                }
                                if (response.getUrls().length > 0) {
                                    remoteControl.setLastFetchUrl(response.getUrls()[0]);
                                }
                            }
                        }
                );
            }
        },
        startLiveview {
            @Override
            void process(final RemoteControl remoteControl, String... args)
                    throws IOException, HttpClient.BadHttpResponseException {
                remoteControl.sendCommand(new StartLiveviewRequest(), new SuccessfulResponseHandler<StartLiveviewResponse>() {
                    @Override
                    public void onSuccess(StartLiveviewResponse response) {
                        remoteControl.info(response.getUrl());
                    }
                });
            }
        },
        stopLiveview {
            @Override
            void process(RemoteControl remoteControl, String... args)
                    throws IOException, HttpClient.BadHttpResponseException {
                remoteControl.sendCommand(new StopLiveviewRequest());
            }
        },
        getEvent {
            @Override
            void process(final RemoteControl remoteControl, String... args)
                    throws IOException, HttpClient.BadHttpResponseException {
                boolean longPolling = false;
                if (args.length > 0) {
                    longPolling = Boolean.parseBoolean(args[0]);
                }
                remoteControl.sendCommand(new GetEventRequest(longPolling), new SuccessfulResponseHandler<GetEventResponse>() {
                    @Override
                    public void onSuccess(GetEventResponse response) {
                        remoteControl.info(response.getEntity(EventEntity.AvailableApiList.class));
                        remoteControl.info(response.getEntity(EventEntity.ShootMode.class));
                        remoteControl.info(response.getEntity(EventEntity.SelfTimer.class));
                        remoteControl.info(response.getEntity(EventEntity.CameraStatus.class));
                        remoteControl.info(response.getEntity(EventEntity.LiveViewStatus.class));
                        remoteControl.info(response.getEntity(EventEntity.PostviewImageSize.class));
                        remoteControl.info(response.getEntity(EventEntity.ZoomInformation.class));
                    }
                });
            }

            @Override
            List<CommandArgument> getArguments() {
                return Arrays.asList(new CommandArgument("longPolling", true));
            }
        },
        getMethodTypes {
            @Override
            void process(final RemoteControl remoteControl, String... args)
                    throws IOException, HttpClient.BadHttpResponseException {
                String version = args.length > 0 ? args[0] : "";
                remoteControl.sendCommand(new GetMethodTypesRequest(version), new SuccessfulResponseHandler<GetMethodTypesResponse>() {
                    @Override
                    public void onSuccess(GetMethodTypesResponse response) {
                        remoteControl.info(response);
                    }
                });
            }

            @Override
            List<CommandArgument> getArguments() {
                return Arrays.asList(new CommandArgument("version", true));
            }
        },
        getExposureMode {
            @Override
            void process(RemoteControl remoteControl, String... args)
                    throws IOException, HttpClient.BadHttpResponseException {
                remoteControl.sendCommand(new GetExposureModeRequest());
            }
        };

        abstract void process(RemoteControl remoteControl, String... args) throws IOException, HttpClient.BadHttpResponseException;

        String getInfo() {
            return "... (see Documentation)";
        }

        final void printInfo(Logger logger) {
            StringBuilder sb = new StringBuilder(name());
            for (CommandArgument argument : getArguments()) {
                sb.append(' ');
                if (argument.optional) {
                    sb.append('[');
                }
                sb.append(argument.name);
                if (argument.optional) {
                    sb.append(']');
                }
            }
            sb.append(" - ").append(getInfo());
            logger.info(sb.toString());
        }

        List<CommandArgument> getArguments() {
            return Collections.emptyList();
        }
    }

    private static class CommandArgument {
        private String name;
        private boolean optional;

        private CommandArgument(String name, boolean optional) {
            this.name = name;
            this.optional = optional;
        }
    }

    interface SuccessfulResponseHandler<Response extends BaseResponse<?>> {
        void onSuccess(Response response);
    }

    private <Response extends BaseResponse<?>> void sendCommand(BaseRequest<?, Response> request)
            throws IOException, HttpClient.BadHttpResponseException {
        sendCommand(request, null);
    }

    private <Response extends BaseResponse<?>> void sendCommand(BaseRequest<?, Response> request,
                                                               SuccessfulResponseHandler<Response> responseHandler)
            throws IOException, HttpClient.BadHttpResponseException {
        Response response = rpcClient.send(request);
        if (response.isOk()) {
            if (responseHandler != null) {
                responseHandler.onSuccess(response);
            } else if (interactive) {
                info("ok");
            }
        } else {
            error("ERROR: " + response.getErrorCode());
            if (!interactive) {
                System.exit(1);
            }
        }
    }

}
