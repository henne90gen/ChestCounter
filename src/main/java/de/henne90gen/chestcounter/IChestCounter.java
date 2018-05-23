package de.henne90gen.chestcounter;

public interface IChestCounter {

	void logError(Exception e);

	void log(String msg);

	String getChestDBFilename();
}
