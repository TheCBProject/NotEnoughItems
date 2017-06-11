package codechicken.nei.api;

/**
 * An nei configuration entry point should implement this class and use @NEIPlugin on the class.
 * loadConfig will only be called when NEI is installed.
 */
public interface IConfigureNEI {

    /**
     * Called during LoadComplete to register things with NEI.
     */
    void loadConfig();

    /**
     * The name used in the Installed plugins list under NEI's Description.
     *
     * @return A name to display.
     */
    String getName();

    /**
     * The version used in the Installed plugins list under NEI's Description.
     *
     * @return A version to display.
     */
    String getVersion();
}
