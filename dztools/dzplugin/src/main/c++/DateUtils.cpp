/*
 * #%L
 * dzplugin
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 juxeii
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

#include "DateUtils.hpp"
#include <boost/date_time/posix_time/posix_time.hpp>
#include <boost/date_time/gregorian/gregorian.hpp>
#include <boost/math/special_functions/modf.hpp>

namespace DateUtils
{

using namespace boost::posix_time;
using namespace boost::gregorian;
using boost::math::modf;

static const ptime epochDATE(date(1899, Dec, 30), time_duration(0, 0, 0));
static const ptime epochPOSIX(date(1970, Jan, 1), time_duration(0, 0, 0));

DATE dateFromDuration(const time_duration duration)
{
    double days = duration.hours()/24.+
                  duration.minutes()/(24.*60.)+
                  duration.seconds()/(24.*60.*60.)+
                  1e-8; // add roughly a millisecond to avoid rounding issues
    return days;
}

time_t tTimefrompTime(const ptime pTime)
{
    time_duration::sec_type seconds = (pTime - epochPOSIX).total_seconds();
    return time_t(seconds);
}

ptime pTimeFromDate(double date)
{
    int dayOffset, hourOffset, minuteOffset, secondOffset;
    double fraction = fabs(modf(date, &dayOffset)) * 24;
    fraction = modf(fraction, &hourOffset) * 60;
    fraction = modf(fraction, &minuteOffset) * 60;
    modf(fraction, &secondOffset);
    ptime pTime(epochDATE);
    pTime += time_duration(hourOffset, minuteOffset, secondOffset);
    pTime += ptime::date_duration_type(dayOffset);

    return pTime;
}

DATE fromDukaTime(const DukaTime dukaTime)
{
    time_t timeT = dukaTime/1000L;
    ptime pTime = from_time_t(timeT);

    return dateFromDuration(pTime - epochDATE);
}

DukaTime fromDATE(const DATE date)
{
    return (DukaTime)1000L* tTimefrompTime(pTimeFromDate(date));
}

std::string formatDateTime(const std::string& format,
                           const ptime& t)
{
    time_facet* output_facet = new time_facet;
    output_facet->format(format.c_str());
    std::ostringstream ss;
    ss.imbue(std::locale(std::locale::classic(), output_facet));
    return static_cast<std::ostringstream&>(ss << t).str();
}

void printDATE(const char* prefix,
               const DATE date)
{
    ptime pTime = pTimeFromDate(date);
    std::string prefixStr(prefix);
    std::string msg = prefixStr + formatDateTime("%d-%m-%Y %H:%M:%S", pTime);

    BrokerError(msg.c_str());
}

void printDukaTime(const char* prefix,
                   const DukaTime dukaTime)
{
    printDATE(prefix, fromDukaTime(dukaTime));
}

} /* namespace DateConversion */

