package com.kingmeter.chargingold.socket.acl;

import com.kingmeter.dto.charging.v1.socket.in.*;
import com.kingmeter.dto.charging.v1.socket.out.BikeInDockResponseDto;
import com.kingmeter.dto.charging.v1.socket.out.LoginPermissionDto;
import com.kingmeter.dto.charging.v1.socket.out.SwingCardUnLockResponseDto;

public interface BusinessService {
    LoginPermissionDto getLoginPermission(SiteLoginRequestDto requestDto);

    void dealWithScanUnLock(ScanUnLockRequestDto requestDto);

    void dealWithScanUnLockII(ScanUnLockIIRequestDto requestDto);

    void dealWithRemoteLock(RemoteLockRequestDto requestDto);

    void offlineNotify(Long deviceId);

    void forceUnlockNotify(ForceUnLockRequestDto requestDto);

    BikeInDockResponseDto createBikeInDockResponseDto(BikeInDockRequestDto requestDto);

    void heartBeatNotify(SiteHeartRequestDto requestDto);

    SwingCardUnLockResponseDto dealWithSwingCardUnlock(SwingCardUnLockRequestDto requestDto);

    void dealWithSwingCardConfirm(SwingCardUnLockRequestConfirmDto requestDto);


    void dealWithQueryDockInfo(QueryDockInfoRequestDto requestDto);

    void queryDockLockStatusNotify(QueryDockLockStatusRequestDto requestDto);

    void malfunctionUploadNotify(DockMalfunctionUploadRequestDto requestDto);
}
