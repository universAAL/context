#if defined(_WIN32) || defined(_WIN64) || defined(__WIN32__) || defined(WIN32)
#define DLLEXP __declspec(dllexport) 
#else
#define DLLEXP
#endif

#include <my_global.h>
#include <my_sys.h>
#include <mysql.h>
#include <m_ctype.h>
#include <m_string.h>
#include <stdlib.h>
#include <windows.h>
#include <winsock2.h>
#include <stdio.h>
#include <ctype.h>

#ifdef HAVE_DLOPEN
#ifdef	__cplusplus
extern "C" {
#endif

#define LIBVERSION "persona_sr_notifier version 0.2.0"

DLLEXP
my_bool notify_sr_init(UDF_INIT *initid, UDF_ARGS *args, char *message);

DLLEXP
void notify_sr_deinit(UDF_INIT *initid);

DLLEXP
long long notify_sr(UDF_INIT *initid, UDF_ARGS *args, char *is_null, char *error);

#ifdef	__cplusplus
}
#endif


my_bool notify_sr_init(UDF_INIT *initid, UDF_ARGS *args, char *message) {
	if (args->arg_count != 1  ||  args->arg_type[0] != STRING_RESULT) {
    strcpy(message, "Wrong arguments to sr_notifier");
    return 1;
  }
  return 0;
}

void notify_sr_deinit(UDF_INIT *initid) {
}

long long notify_sr(UDF_INIT *initid, UDF_ARGS *args, char *is_null, char *error) {
	long rc;
	SOCKET s;
	SOCKADDR_IN addr;
	WSADATA wsa;
	const char *query_str = args->args[0];

	if (query_str == 0)
		return 0;
	
	// start winsock
	rc = WSAStartup(MAKEWORD(2, 0), &wsa);
	if (rc != 0) {
		sprintf(error, "Error: winsock startup, error code: %d\n", rc);
		return 1;
	}

	// create socket 
	s = socket(AF_INET, SOCK_STREAM, 0);
	if (s == INVALID_SOCKET) {
		sprintf(error, "Error: could not create socket, error code: %d\n", WSAGetLastError());
		return 1;
	}

	// prepare address
	memset(&addr, 0, sizeof(SOCKADDR_IN)); // first set all to 0
	addr.sin_family = AF_INET;
	addr.sin_port = htons(3309); // port number of the sr is 3309
	addr.sin_addr.s_addr = inet_addr("127.0.0.1"); // using the local machine

	// connect
	rc = connect(s, (SOCKADDR*) &addr, sizeof(SOCKADDR));
	if (rc == SOCKET_ERROR)	{
		sprintf(error, "Error: connect failed, error code: %d\n", WSAGetLastError());
		return 1;
	}

	// send the query str to the sr
	send(s, query_str, strlen(query_str), 0);

	// close connection
	closesocket(s);
	WSACleanup();

	return 0;
}

#endif /* HAVE_DLOPEN */
