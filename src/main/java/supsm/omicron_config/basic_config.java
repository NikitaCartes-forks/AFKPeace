package supsm.omicron_config;

public interface basic_config
{
    public default void save()
    {
        omicron_config.save(this);
    }

    public String getName();

    public default String getModid()
    {
        return null;
    }

    public default String getExtension()
    {
        return "json";
    }

    public default String getDirectory()
    {
        return "";
    }
}
