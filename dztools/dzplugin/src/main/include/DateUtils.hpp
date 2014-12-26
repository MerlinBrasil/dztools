#ifndef DATECONVERSION_HPP
#define DATECONVERSION_HPP

#include "dukazorrobridge.hpp"

namespace DateUtils
{

DATE fromDukaTime(const DukaTime dukaTime);

DukaTime fromDATE(const DATE date);

void printDATE(const char* prefix,
               const DATE date);

void printDukaTime(const char* prefix,
                   const DukaTime dukaTime);

} /* namespace DateConversion */

#endif /* DATECONVERSION_HPP */
