
#include <stdio.h>
#include <Shlwapi.h>
#include <Shlobj.h>
#include <iostream>
#include <winbase.h>
#include <winnt.h>
#include <time.h>
#include <string>
#include <regex>
using namespace std;


DWORD AdjustPrivs(HANDLE hToken, PTOKEN_PRIVILEGES pPrivs, DWORD cbPrivs)
{
	DWORD	i;
	LUID	luidChangeNotify;
	BOOL	bSuccess = FALSE;

	LookupPrivilegeValue(NULL, SE_SYSTEMTIME_NAME, &luidChangeNotify);

	for (i = 0; i<pPrivs->PrivilegeCount; ++i)
	{
		if (pPrivs->Privileges[i].Luid.HighPart == luidChangeNotify.HighPart && pPrivs->Privileges[i].Luid.LowPart == luidChangeNotify.LowPart)
		{
			//  SE_SYSTEMTIME_NAME
		}
		else
		{
			//  SeXXXPrivilege
			pPrivs->Privileges[i].Attributes = SE_PRIVILEGE_REMOVED;
		}
	}

	bSuccess = AdjustTokenPrivileges(hToken, FALSE, pPrivs, cbPrivs, NULL, NULL);

	return bSuccess ? ERROR_SUCCESS : GetLastError();
}

void UnixTimeToFileTime(time_t t, LPFILETIME pft)
{
	// Note that LONGLONG is a 64-bit value
	LONGLONG ll;

	ll = Int32x32To64(t, 10000000) + 116444736000000000;
	pft->dwLowDateTime = (DWORD)ll;
	pft->dwHighDateTime = ll >> 32;
}

void UnixTimeToSystemTime(time_t t, LPSYSTEMTIME pst)
{
	FILETIME ft;

	UnixTimeToFileTime(t, &ft);
	FileTimeToSystemTime(&ft, pst);
}


int main(int argc, char* argv[]) {

	BOOL				bResult = FALSE;
	DWORD				dwError = ERROR_SUCCESS;
	DWORD				cbNeeded = 0;
	HANDLE				hToken = NULL;
	PTOKEN_PRIVILEGES	pPrivs = NULL;

	bResult = IsUserAnAdmin();

	if (!bResult) {
		exit(1);
	}

	bResult = OpenProcessToken(GetCurrentProcess(), TOKEN_QUERY | TOKEN_ADJUST_PRIVILEGES, &hToken);
	if (bResult)
	{
		bResult = GetTokenInformation(hToken, TokenPrivileges, NULL, 0, &cbNeeded);
		if (!bResult)
		{
			dwError = GetLastError();
			if (dwError == ERROR_INSUFFICIENT_BUFFER)
			{
				bResult = TRUE;
				dwError = 0;
			}
		}

		if (!dwError)
		{
			pPrivs = (PTOKEN_PRIVILEGES)malloc(cbNeeded);

			if (pPrivs)
			{
				bResult = GetTokenInformation(hToken, TokenPrivileges, (LPVOID)pPrivs, cbNeeded, &cbNeeded);

				if (bResult)
				{
					AdjustPrivs(hToken, pPrivs, cbNeeded);
				}

				free(pPrivs);
			}
		}

		CloseHandle(hToken);
	}

	if (argc != 2) {
		exit(1);
	}

	regex timestamp("[[:digit:]]{10}");

	bResult = regex_match(argv[1], timestamp);

	if (!bResult) {
		exit(1);
	}


	time_t epoch = stoi(argv[1]);
	SYSTEMTIME newSystime;

	UnixTimeToSystemTime(epoch, &newSystime);

	SetSystemTime(&newSystime);


	exit(0);
}