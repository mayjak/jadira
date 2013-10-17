/*
 *  Copyright 2010, 2011 Christopher Pheby
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.jadira.usertype.dateandtime.threetenbp.columnmapper;

import static org.jadira.usertype.dateandtime.threetenbp.utils.ZoneHelper.getDefaultZoneOffset;

import java.sql.Timestamp;
import java.util.TimeZone;

import org.jadira.usertype.spi.shared.AbstractVersionableTimestampColumnMapper;
import org.jadira.usertype.spi.shared.DatabaseZoneConfigured;
import org.jadira.usertype.spi.shared.JavaZoneConfigured;
import org.threeten.bp.Instant;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.DateTimeFormatterBuilder;
import org.threeten.bp.temporal.ChronoField;

/**
 * Maps a precise datetime column for storage. The UTC Zone will be used to store the value
 */
public class TimestampColumnZonedDateTimeMapper extends AbstractVersionableTimestampColumnMapper<ZonedDateTime> implements DatabaseZoneConfigured<ZoneOffset>, JavaZoneConfigured<ZoneOffset> {

    private static final long serialVersionUID = -7670411089210984705L;

    public static final DateTimeFormatter DATETIME_FORMATTER = new DateTimeFormatterBuilder().appendPattern("yyyy-MM-dd HH:mm:ss").appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, false).toFormatter();

	private static final int MILLIS_IN_SECOND = 1000;

    private ZoneOffset databaseZone = ZoneOffset.UTC;

    private ZoneOffset javaZone = null;

	public TimestampColumnZonedDateTimeMapper() {
	}

	public TimestampColumnZonedDateTimeMapper(ZoneOffset databaseZone, ZoneOffset javaZone) {
		this.databaseZone = databaseZone;
		this.javaZone = javaZone;
	}
    
    @Override
    public ZonedDateTime fromNonNullString(String s) {
        return ZonedDateTime.parse(s);
    }

    @Override
    public ZonedDateTime fromNonNullValue(Timestamp value) {

        ZoneOffset currentDatabaseZone = databaseZone == null ? getDefaultZoneOffset() : databaseZone;
        ZoneOffset currentJavaZone = javaZone == null ? getDefaultZoneOffset() : javaZone;

        int adjustment = TimeZone.getDefault().getOffset(value.getTime()) - (currentDatabaseZone.getTotalSeconds() * MILLIS_IN_SECOND);
        
        ZonedDateTime dateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(value.getTime() + adjustment), currentDatabaseZone);
        dateTime = dateTime.with(ChronoField.NANO_OF_SECOND, value.getNanos()).withZoneSameInstant(currentJavaZone);
        
        return dateTime;
    }

    @Override
    public String toNonNullString(ZonedDateTime value) {
        return value.toString();
    }

    @Override
    public Timestamp toNonNullValue(ZonedDateTime value) {

        ZoneOffset currentDatabaseZone = databaseZone == null ? getDefaultZoneOffset() : databaseZone;        
        int adjustment = TimeZone.getDefault().getOffset(value.toEpochSecond() * MILLIS_IN_SECOND) - (currentDatabaseZone.getTotalSeconds() * MILLIS_IN_SECOND);
        
        final Timestamp timestamp = new Timestamp((value.toEpochSecond() * MILLIS_IN_SECOND) - adjustment);
        timestamp.setNanos(value.getNano());
        return timestamp;    	
    }

    @Override
    public void setDatabaseZone(ZoneOffset databaseZone) {
        this.databaseZone = databaseZone;
    }
    
    @Override
    public void setJavaZone(ZoneOffset javaZone) {
        this.javaZone = javaZone;
    }
        
	@Override
	public ZoneOffset parseZone(String zoneString) {
		return ZoneOffset.of(zoneString);
	}
}
