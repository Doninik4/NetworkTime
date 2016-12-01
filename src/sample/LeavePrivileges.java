package sample;

import com.sun.jna.platform.win32.*;
import com.sun.jna.ptr.IntByReference;

/**
 * Created by Dominik on 01.12.2016.
 */
public class LeavePrivileges {

    public void leaveAllPrivileges(){

        boolean bResult = false;
        int dwError = W32Errors.ERROR_SUCCESS;
        IntByReference cbNeeded = new IntByReference();
        cbNeeded.setValue(0);
        WinNT.HANDLEByReference hToken = new WinNT.HANDLEByReference();
        WinNT.TOKEN_PRIVILEGES pPrivs = null;


        bResult = Advapi32.INSTANCE.OpenProcessToken(Kernel32.INSTANCE.GetCurrentProcess(),
                WinNT.TOKEN_ADJUST_PRIVILEGES | WinNT.TOKEN_QUERY, hToken);


        if (bResult) {

            bResult = Advapi32.INSTANCE.GetTokenInformation(hToken.getValue(), WinNT.TOKEN_INFORMATION_CLASS.TokenPrivileges, null, 0, cbNeeded);

            if (!bResult) {

                dwError = Kernel32.INSTANCE.GetLastError();
                if (dwError == W32Errors.ERROR_INSUFFICIENT_BUFFER) {
                    bResult = true;
                    dwError = 0;
                }
            }

            if (dwError == 0) {

                pPrivs = new WinNT.TOKEN_PRIVILEGES(cbNeeded.getValue());

                if (pPrivs != null) {

                    bResult = Advapi32.INSTANCE.GetTokenInformation(hToken.getValue(), WinNT.TOKEN_INFORMATION_CLASS.TokenPrivileges, pPrivs, cbNeeded.getValue(), cbNeeded);

                    if (bResult) {
                        for (int i = 0; i < pPrivs.PrivilegeCount.intValue(); i++) {
                            pPrivs.Privileges[i].Attributes = new WinDef.DWORD(WinNT.SE_PRIVILEGE_REMOVED);
                        }

                        bResult = Advapi32.INSTANCE.AdjustTokenPrivileges(hToken.getValue(), false, pPrivs, pPrivs.size(), null, null);

                        if(!bResult){
                            System.out.println("unable to leave privileges");
                        }
                    }
                }

            }

            Kernel32.INSTANCE.CloseHandle(hToken.getValue());

        }


    }

}
