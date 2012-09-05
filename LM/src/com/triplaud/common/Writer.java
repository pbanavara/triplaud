/**
 * All data copyrights of SocialEyez.co
 *
 */
package com.triplaud.common;

/**
 * @author pradeep
 * Interface for writing objects into any of the chosen Android storage medium - file/SQLite or whatever
 */
public interface Writer {
    public void writeData(String data);

}
