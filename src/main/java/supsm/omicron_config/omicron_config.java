package supsm.omicron_config;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import net.fabricmc.loader.api.FabricLoader;

public class omicron_config
{
    private static final Logger LOGGER = LogManager.getLogger();

	private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static int comment_count = 0;

    private static ArrayList<Object> modify_list(ArrayList<Object> a)
    {
        ArrayList<Object> new_a = new ArrayList<Object>();
        for (Object o : a)
        {
            if (o instanceof Map)
            {
                // TODO: figure out fields situation
                new_a.add(modify_map((LinkedHashMap)o, new HashMap<String, Field>()));
            }
            else if (o instanceof List)
            {
                new_a.add(modify_list((ArrayList)o));
            }
            else
            {
                new_a.add(o);
            }
        }
        return new_a;
    }

    // prepend all keys with _ and add comments as keys (numbers without understore prefix)
    private static LinkedHashMap<String, Object> modify_map(LinkedHashMap<String, Object> m, Map<String, Field> fields)
    {
        LinkedHashMap<String, Object> new_map = new LinkedHashMap<String, Object>();
        for (Map.Entry<String, Object> e : m.entrySet())
        {
            Object v = e.getValue();
            String k = e.getKey();
            Field f = fields.get(k);
            if (f != null)
            {
                for (Annotation a : f.getAnnotations())
                {
                    if (a instanceof Comment)
                    {
                        new_map.put(String.valueOf(comment_count), ((Comment)a).value());
                        comment_count++;
                    }
                }
            }

            // recurse
            if (v instanceof Map)
            {
                Map<String, Field> new_fields = new HashMap<String, Field>();
                for (Field new_f : f.getClass().getDeclaredFields())
                {
                    new_fields.put(new_f.getName(), new_f);
                }
                new_map.put("_" + k, modify_map((LinkedHashMap)v, new_fields));
            }
            else if (v instanceof List)
            {
                new_map.put("_" + k, modify_list((ArrayList)v));
            }
            else
            {
                new_map.put("_" + k, v);
            }
        }
        return new_map;
    }

    public static final <T extends basic_config> void save(T obj)
    {
        String s;

        // replace all key/value pairs not starting with underscore with a comment
        // and add comments as keys with numbers
        {
            LinkedHashMap<String, Object> map_with_comments;
            s = gson.toJson(obj);
            LinkedHashMap<String, Object> m = new LinkedHashMap<String, Object>();
            m = gson.fromJson(s, m.getClass());

            Map<String, Field> fields = new HashMap<String, Field>();
            for (Field f : obj.getClass().getDeclaredFields())
            {
                fields.put(f.getName(), f);
            }
            comment_count = 0;
            map_with_comments = modify_map(m, fields);

            // output to string
            s = gson.toJson(map_with_comments);
        }

        // string processing
        {
            // escape */ characters by changing to * /
            Pattern end_comment = Pattern.compile(
                "(\\\"\\d+\\\"\\s*:\\s*\\\"(?:\\\\.|[^\\\"\\\\])*)\\*\\/(?=(?:\\\\.|[^\\\"\\\\])*\\\")",
                Pattern.MULTILINE);
            Matcher m = end_comment.matcher(s);
            while (m.find())
            {
                s = m.replaceAll("$1* /");
                m = end_comment.matcher(s);
            }

            // turn comments into /* comments */
            Pattern comment_json = Pattern.compile(
                "\\\"\\d+\\\"\\s*:\\s*\\\"((?:\\\\.|[^\\\"\\\\])*)\\\",",
                Pattern.MULTILINE
            );
            s = comment_json.matcher(s).replaceAll("/* $1 */");
        
            // remove underscore from beginning of keys
            Pattern underscore_json_pair = Pattern.compile(
                "\\\"_((?:\\\\.|[^\\\"\\\\])*)\\\"(?=\\s*:\\s*(?:\\\"(?:\\\\.|[^\\\"\\\\])*\\\"|true|false|-?\\d(?:\\.\\d+)?|\\[|\\{))",
                Pattern.MULTILINE
            );
            s = underscore_json_pair.matcher(s).replaceAll("\"$1\"");
        }

        // output
        Path config = config_path((basic_config)obj);
        config.toFile().getParentFile().mkdirs();
        try
        {
            Files.write(config, s.getBytes());
        }
        catch (IOException e)
        {
            LOGGER.error(e);
        }
    }

    private static String strip_comments(String json)
    {
        Boolean in_string = false;
        Boolean in_multiline_comment = false;
        Boolean escaped = false;

        String new_str = new String();

        for (int i = 0; i < json.length(); i++)
        {
            // check if quotations are escaped
            if (in_string)
            {
                if (json.charAt(i) == '\\')
                {
                    escaped = !escaped;
                }
                else
                {
                    escaped = false;
                }
            }
            // check for comments if not in string
            else
            {
                if (!in_multiline_comment)
                {
                    // don't access chars past last char
                    if (i + 1 < json.length())
                    {
                        // start of multiline comment
                        if (json.charAt(i) == '/' && json.charAt(i + 1) == '*')
                        {
                            in_multiline_comment = true;
                        }
                        // start of single line comment
                        else if (json.charAt(i) == '/' && json.charAt(i + 1) == '/')
                        {
                            i = json.indexOf('\n', i); // skip to next newline
                        }
                    }
                }
                else
                {
                    if (i > 0)
                    {
                        // end of multiline comment
                        if (json.charAt(i - 1) == '*' && json.charAt(i) == '/')
                        {
                            in_multiline_comment = false;
                            continue;
                        }
                    }
                }
            }

            if (json.charAt(i) == '\"')
            {
                in_string = !in_string;
            }

            // copy chars to new_str if not inside a comment
            if (!in_multiline_comment)
            {
                new_str += json.charAt(i);
            }
        }
        return new_str;
    }

    private static Path config_path(basic_config c)
    {
        return Paths.get(
            FabricLoader.getInstance().getConfigDir().toString(),
            c.getDirectory(),
            c.getName() + "." + c.getExtension());
    }

    public static <T extends basic_config> T register(Class<T> c)
    {
        try
        {
            T config = c.getConstructor().newInstance();
            Path p = config_path(config);
            if (Files.exists(p))
            {
                try
                {
                    // read from file
                    String s = Files.readString(p);
                    config = gson.fromJson(strip_comments(s), c);
                }
                catch (IOException e)
                {
                    LOGGER.error(e);
                    LOGGER.warn("Read from config file " + p.toString() + " failed, using default values");
                }
                catch (JsonSyntaxException e)
                {
                    LOGGER.error(e);
                    LOGGER.warn("JSON parsing failed for config file " + p.toString() + ", using default values");
                }

                // in case config class has changed since it was last saved
                config.save();
            }
            else
            {
                // write default config to disk
                config.save();
            }
            return config;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException("Registering config class " + c.getName() + " failed");
        }
    }
}
