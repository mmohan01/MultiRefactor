/*
 * HumanResourceManager.java
 *
 * Created on 27. Mai 2003, 22:13
 */

package net.sourceforge.ganttproject.resource;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author  barmeier
 */
public class HumanResourceManager implements ResourceManager {
    
    private List myViews = new ArrayList();    
    private  ArrayList resources=new ArrayList();
    private  int nextFreeId=0;
    

    public ProjectResource create(String name, int i) {
        HumanResource hr=new HumanResource(name,i);
        add(hr);
        return (hr);
    }
    
    
    public ProjectResource create(String name) {
        HumanResource hr=new HumanResource(name,-1);
        add(hr);
        return (hr);
    }
    
    public  void add(ProjectResource resource) {
        if (resource.getId() == -1) 
            resource.setId(nextFreeId);
        if (resource.getId() >= nextFreeId) // this is a really simple mech, but it should do the job.
            nextFreeId=resource.getId()+1;
        resources.add (resource);
        fireResourceAdded(resource);
    }
    
    public  ProjectResource getById(int id) {
        // Linear search is not really efficent, but we dont have so many resources !?
        ProjectResource pr=null;
        for (int i=0; i<resources.size(); i++)
            if (((ProjectResource) resources.get(i)).getId()==id) {
                pr=(ProjectResource) resources.get(i);
                break;
            }
        return pr;
    }
    
    public  ArrayList getResources() {
        return resources;
    }
    
//    public ArrayList load(InputStream source) {
//        XMLReader reader;
//        
//        try {
//            reader = XMLReaderFactory.createXMLReader (
//                "org.apache.crimson.parser.XMLReaderImpl");
//        }
//        catch (Exception e) {
//            System.out.println ("Opening parser failed:"+e.toString());
//            return null;
//        }
//        
//        ContentHandler humanResourceLoader = new HumanResourceLoader(this);
//        reader.setContentHandler(humanResourceLoader);
//        
//        resources.clear();
//        
//        try {
//            reader.parse(new InputSource(source));
//        }
//        catch (Exception e) {
//            System.out.println ("Opening parser failed:"+e.toString());
//            return null;
//        }
//        return resources;
//      
//    }
    
    public void remove(ProjectResource resource) {
        fireResourcesRemoved (new ProjectResource[] {resource});
        resources.remove(resource);
    }
    
    public void removeById(int id) {
        ProjectResource pr = getById(id);
        if (pr!=null)
            remove(pr);
    }
    
    public void save(OutputStream target) {
    }
    

    public void clear() {
        fireCleanup();
        resources.clear();
    }
    

    public void addView(ResourceView view) {
        myViews.add(view);
    }
    
    private void fireResourceAdded(ProjectResource resource) {
        ResourceEvent e = new ResourceEvent(this, resource);   
    	for (Iterator i = myViews.iterator(); i.hasNext();) {
    		ResourceView nextView = (ResourceView)i.next();
    		nextView.resourceAdded(e);
    	}
    }

    private void fireResourcesRemoved(ProjectResource[] resources) {
        ResourceEvent e = new ResourceEvent(this, resources);
        for (int i=0; i<myViews.size(); i++) {
            ResourceView nextView = (ResourceView)myViews.get(i);
            nextView.resourcesRemoved(e);
        }
    }

    private void fireCleanup() {
        fireResourcesRemoved((ProjectResource[])resources.toArray(new ProjectResource[resources.size()]));
    }
	
	/** Move up the resource number index*/
	public void up(int index) {
		HumanResource human = (HumanResource)resources.remove(index);
		resources.add(index-1, human);
	}
	
	/** Move down the resource number index*/
	public void down(int index) {
		HumanResource human = (HumanResource)resources.remove(index);
		resources.add(index+1, human);

	}
}
