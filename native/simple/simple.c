#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "simple.h"

char* concat(char* str1, char* str2) {
    size_t total_length = strlen(str1) + strlen(str2) + 1;  // +1 for the null-terminator
    char *result = (char *)malloc(total_length);

    if (result == NULL) {
        return NULL;
    }
    sprintf(result, "%s%s", str1, str2);
    return result;
}
