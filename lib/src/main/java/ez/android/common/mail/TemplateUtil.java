package ez.android.common.mail;

import com.samskivert.mustache.Mustache;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

/**
 * Template utilities
 */
public class TemplateUtil {

    /**
     * Get content
     * @param template
     * @param params
     * @return
     */
    public static String getContent(String template, Object params) {
        return Mustache.compiler().compile(template).execute(params);
    }

    /**
     * Get content
     * @param templateFile
     * @param params
     * @return
     */
    public static String getContent(File templateFile, Object params) {
        Mustache.Compiler c = Mustache.compiler().withLoader(name -> {
            try {
                return new FileReader(templateFile);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
        String tmpl = "...{{>subtmpl}}...";
        return c.compile(tmpl).execute(params);
    }

}
