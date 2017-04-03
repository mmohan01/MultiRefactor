/*
 * ResourceManager.java
 *
 * Created on 27. Mai 2003, 08:08
 */

package net.sourceforge.ganttproject.resource;

import java.io.OutputStream;
import java.util.ArrayList;

/** This interface is used to isolate the implementation of a resource manager from
 * the application. The interface is defined against an abstract class the
 * ProjectResource class.
 * Normally only one instance of the Resourcemanager should be instantiated.
 * @author barmeier
 */
public interface ResourceManager {
    /** The only thing that must be known when creating a new resource
     * is the name of the resource. This methods creates a ProjectResource
     * conform object, sets the id to an unused value and sets the name to the name
     * given by the parameter <i>name</I> after alle the object will be added to
     * the list of available resources.
     * The methods returns the generated object or null when something fails. <b> BE
     * AWARE</B> Null Pointer Exceptions are waiting here ;)
     * @see ProjectResource
     * @param name The name of the resource that should be added.
     * @return A ProjectResource object with name and id set.
     */    
    public ProjectResource create (String name);
    public ProjectResource create (String name,int i);
    /** Adds the resource to the internal list of available resources.
     * @param resource The resource that should be added to the list.
     */    
    public void add (ProjectResource resource);
    /** Retrieves an ancestor of ProjectResource identified by an identity value.
     * @param id The id is an integer value that is unique for every resource.
     * @return Ancestor of ProjectResource containing the requested resource.
     * @see ProjectResource
     */    
    public ProjectResource getById (int id);
    /** Removes the resource.
     * @param resource The resource to remove.
     */    
    public void remove (ProjectResource resource);
    /** Removes the resource by its id.
     * @param Id Id of the resource to remove.
     */    
    public void removeById (int Id);
    /** Retrieves a list of all resources available.
     * @return ArrayList filled with ProjectResource ancestors.
     * @see ProjectResource
     */    
    public ArrayList getResources ();
    /** Loads resources from the InputStreamReader. All resources already stored in the
     * Resourcemanager are lost and will be replace with the resources loaded from the
     * stream.
     * @return The ArrayLisr returned contains all ProjectResource ancestor
     * that were read from the InputStreamReader.
     * @param source The InputStreamReader from which the data will be read.
     * The format and kind of data read is subject of the class
     * implementing this interface.
     */    
    //public ArrayList load (InputStream source);
    /** Writes all resources stored in the OutputStreamWriter. The format and kind of
     * data written in the stream are subject of the class that implements this
     * interface.
     * @param target Stream to write the data to.
     */    
    public void save (OutputStream target);    

    
    /** Removes all resources from the manager. */    
    public void clear();

    
    /**
     * Adds a new view of this manager
     * @param view
     */
    public void addView(ResourceView view);

}
