package fr.cls.atoll.motu.metadata;

/**
 * Documentation product class.
 * 
 * @author $Author: dearith $
 * @version $Revision: 1.1 $ - $Date: 2009-02-20 13:00:26 $
 */
public class DocMetaData {

    /**
     * Default constructor.
     */
    public DocMetaData() {

    }

    // CSOFF: StrictDuplicateCode : normal duplication code.

    /**
     * Title of the document.
     * 
     * @uml.property name="title"
     */
    private String title = "";

    /**
     * Getter of the property <tt>title</tt>.
     * 
     * @return Returns the title.
     * @uml.property name="title"
     */
    public String getTitle() {
        return title;
    }

    /**
     * Setter of the property <tt>title</tt>.
     * 
     * @param title The title to set.
     * @uml.property name="title"
     */
    public void setTitle(String title) {
        this.title = title;
    }

    // CSON: StrictDuplicateCode

    /**
     * online (eg. http) resource where the document is available.
     * 
     * @uml.property name="resource"
     */
    private String resource = "";

    /**
     * Getter of the property <tt>resource</tt>.
     * 
     * @return Returns the resource.
     * @uml.property name="resource"
     */
    public String getResource() {
        return resource;
    }

    /**
     * Setter of the property <tt>resource</tt>.
     * 
     * @param resource The resource to set.
     * @uml.property name="resource"
     */
    public void setResource(String resource) {
        this.resource = resource;
    }

    /**
     * Date of document's publication.
     * 
     * @uml.property name="publication"
     */
    private String publication = "";

    /**
     * Getter of the property <tt>publication</tt>.
     * 
     * @return Returns the publication.
     * @uml.property name="publication"
     */
    public String getPublication() {
        return publication;
    }

    /**
     * Setter of the property <tt>publication</tt>.
     * 
     * @param publication The publication to set.
     * @uml.property name="publication"
     */
    public void setPublication(String publication) {
        this.publication = publication;
    }

}
