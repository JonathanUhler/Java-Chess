#include <inttypes.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

int main()
{
    // Args
    const char *s = "The quick brown fox jumps over the lazy dog";
    size_t len = strlen(s); // Length of string as java agrees
    
    // Func
    uint8_t octet;
    const char *p, *q; // Intially both are "(null)"
    q = s + len; // MARK: q is set to nothing? Prints as "". What even happens when you add char* and size_t?
    printf("Q: %s\nP: %s\nLEN: %d\nS: %s\n\n", q, p, len, s);

	// CONCEPT: p is a string, initially set to the full string passed in (s). Every time through the loop,
	//          one character from the front of p is removed. The first string == s and the last string is
	//          the last character of s.
	//          Example:
	//           s = "Hello"
	//           for {
	//            p = ["Hello" -> "ello" -> "llo" -> "lo" -> "o"]
	//           }
    for (p = s; p < q; p++) {
        octet = *p;

		// CONCEPT: the value of *p as a character is the current first character of p as a string. The value
		//          of *p as an integer is the same as octet which is the ascii value of *p as a character
		//          Example:
		//           s = "Hello"
		//           for {
		//            p  = ["Hello" -> "ello" -> "llo" -> "lo" -> "o"]
		//            *p = ["H"     -> "e"    -> "l"   -> "l"  -> "o"]
		//           }
        printf("OCTET: %d\t*P: %c\t\tP: %s\n", octet, *p, p);
    }

    return 0;
}
