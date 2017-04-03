package net.sourceforge.ganttproject.task;

import net.sourceforge.ganttproject.resource.ProjectResource;

import java.util.*;

class ResourceAssignmentCollectionImpl implements ResourceAssignmentCollection {
    private final Map myAssignments = new HashMap();
    private final TaskImpl myTask;

    public ResourceAssignmentCollectionImpl(TaskImpl task) {
        myTask = task;
    }

    private ResourceAssignmentCollectionImpl(ResourceAssignmentCollectionImpl collection) {
        myTask = collection.myTask;
        ResourceAssignment[] assignments = collection.getAssignments();
        for (int i=0; i<assignments.length; i++) {
            ResourceAssignment next = assignments[i];
            ResourceAssignment copy = new ResourceAssignmentImpl(next.getResource());
            copy.setLoad(next.getLoad());
            addAssignment(copy);
        }
    }

    public ResourceAssignment[] getAssignments() {
        return (ResourceAssignment[]) myAssignments.values().toArray(new ResourceAssignment[myAssignments.size()]);
    }

    public ResourceAssignment getAssignment(ProjectResource resource) {
        return (ResourceAssignment) myAssignments.get(resource);
    }

    public ResourceAssignmentMutator createMutator() {
        return new ResourceAssignmentMutatorImpl();
    }

    ResourceAssignmentCollectionImpl copy() {
        return new ResourceAssignmentCollectionImpl(this);
    }

    public ResourceAssignment addAssignment(ProjectResource resource) {
        return auxAddAssignment(resource);
    }

    public void deleteAssignment(ProjectResource resource) {
        myAssignments.remove(resource);
    }

    private ResourceAssignment auxAddAssignment(ProjectResource resource) {
        final ResourceAssignment result = new ResourceAssignmentImpl(resource);
        addAssignment(result);
        return result;
    }

    private void addAssignment(ResourceAssignment assignment) {
        myAssignments.put(assignment.getResource(), assignment);
    }

    private Task getTask() {
        return myTask;
    }

    private class ResourceAssignmentImpl implements ResourceAssignment {
        private final ProjectResource myResource;
        private float myLoad;

        public ResourceAssignmentImpl(ProjectResource resource) {
            myResource = resource;
        }

        public Task getTask() {
            return ResourceAssignmentCollectionImpl.this.getTask();
        }

        public ProjectResource getResource() {
            return myResource;
        }

        public float getLoad() {
            return myLoad;
        }

        //todo: transaction
        public void setLoad(float load) {
            myLoad = load;
        }

        public void delete() {
            ResourceAssignmentCollectionImpl.this.deleteAssignment(getResource());
        }
    }

    private class ResourceAssignmentMutatorImpl implements ResourceAssignmentMutator {
        private Map myQueue = new HashMap();

        public ResourceAssignment addAssignment(ProjectResource resource) {
            ResourceAssignment result = new ResourceAssignmentImpl(resource);
            myQueue.put(resource, new MutationInfo(result, MutationInfo.ADD));
            return result;
        }

        public void deleteAssignment(ProjectResource resource) {
            MutationInfo info = (MutationInfo) myQueue.get(resource);
            if (info==null) {
                myQueue.put(resource, new MutationInfo(resource, MutationInfo.DELETE));
            }
            else if (info.myOperation==MutationInfo.ADD) {
                myQueue.remove(resource);
            }
        }

        public void commit() {
            List mutations = new ArrayList(myQueue.values());
            Collections.sort(mutations);
            for (int i=0; i<mutations.size(); i++) {
                MutationInfo next = (MutationInfo) mutations.get(i);
                switch (next.myOperation) {
                    case MutationInfo.DELETE:
                        {
                            myAssignments.remove(next.myResource);
                            break;
                        }
                    case MutationInfo.ADD:
                        {
                            ResourceAssignment result = auxAddAssignment(next.myResource);
                            result.setLoad(next.myAssignment.getLoad());
                        }
                    default:
                        break;
                }
            }
        }

    }

    private static class MutationInfo implements Comparable {
        static final int ADD = 0;
        static final int DELETE = 1;
        private final ResourceAssignment myAssignment;
        private final int myOrder;
        private static int ourOrder;
        private int myOperation;
        private final ProjectResource myResource;

        public MutationInfo(ResourceAssignment assignment, int operation) {
            myAssignment = assignment;
            myOrder = ourOrder++;
            myOperation = operation;
            myResource = assignment.getResource();
        }

        public MutationInfo(ProjectResource resource, int operation) {
            this.myAssignment = null;
            this.myOrder = ourOrder++;
            this.myOperation = operation;
            this.myResource = resource;
        }

        public boolean equals(Object o) {
            boolean result = o instanceof MutationInfo;
            if (result) {
                result = myAssignment.getResource().equals(((MutationInfo)o).myAssignment.getResource());
            }
            return result;
        }

        public int compareTo(Object o) {
            if (!(o instanceof MutationInfo)) {
                throw new IllegalArgumentException();
            }
            return myOrder - ((MutationInfo)o).myOrder;
        }
    }

}
