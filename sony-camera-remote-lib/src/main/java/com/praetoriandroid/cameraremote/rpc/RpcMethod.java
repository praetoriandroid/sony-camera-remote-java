package com.praetoriandroid.cameraremote.rpc;

@SuppressWarnings("UnusedDeclaration")
public enum RpcMethod {
    getApplicationInfo, // +
    getMethodTypes, // +
    getAvailableApiList, // +
    getVersions, // +

    getEvent, // +

    startRecMode, // +
    stopRecMode, // +

    actTakePicture, // +
    awaitTakePicture, // +

    //startLiveviews, ??
    startLiveview, // +
    stopLiveview, // +
    startLiveviewWithSize,
    getLiveviewSize,
    getAvailableLiveviewSize,
    getSupportedLiveviewSize,

    actZoom,

    setSelfTimer, // +
    getSelfTimer, // +
    getAvailableSelfTimer, // +
    getSupportedSelfTimer, // +

    setExposureMode,
    getExposureMode,
    getAvailableExposureMode,
    getSupportedExposureMode,
    setExposureCompensation,
    getExposureCompensation,
    getAvailableExposureCompensation,
    getSupportedExposureCompensation,

    getFNumber,
    getAvailableFNumber,
    getSupportedFNumber,

    setIsoSpeedRate,
    getIsoSpeedRate,
    getAvailableIsoSpeedRate,
    getSupportedIsoSpeedRate,

    setPostviewImageSize,
    getPostviewImageSize,
    getAvailablePostviewImageSize,
    getSupportedPostviewImageSize,

    setProgramShift,
    getSupportedProgramShift,

    setShootMode,
    getShootMode,
    getAvailableShootMode,
    getSupportedShootMode,

    getShutterSpeed,
    getAvailableShutterSpeed,
    getSupportedShutterSpeed,

    setTouchAFPosition,
    getTouchAFPosition,

    setWhiteBalance,
    getWhiteBalance,
    getSupportedWhiteBalance,
    getAvailableWhiteBalance,

    startMovieRec, // +
    stopMovieRec, // +

    startAudioRec,
    stopAudioRec,
}
