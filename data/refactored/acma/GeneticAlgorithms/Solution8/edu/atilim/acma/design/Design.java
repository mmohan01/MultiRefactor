package edu.atilim.acma.design;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import edu.atilim.acma.metrics.MetricCalculator;
import edu.atilim.acma.metrics.MetricTable;
import edu.atilim.acma.transition.TransitionManager;
import edu.atilim.acma.transition.actions.Action;

public class Design implements Externalizable {
    private String name;
    private Object tag;
    private ArrayList<Type> types;
    private ArrayList<String> modificationLog;

    public Object getTag() {
        return tag;
    }

    public void setTag(Serializable tag) {
        this.tag = tag;
    }

    public List<Type> getTypes() {
        return Collections.unmodifiableList(types);
    }

    public void logModification(String log) {
        modificationLog.add(log);
    }

    public MetricTable getMetrics() {
        return MetricCalculator.calculate(this);
    }

    public List<String> getModifications() {
        return Collections.unmodifiableList(modificationLog);
    }

    public Type getType(String name) {
        for (int i = 0; i < types.size(); i++)
         {
            Type t = types.get(i);

            if (t.getName().equals(name))
                return t;
        }
        return null;
    }

    void removeType(Type t) {
        types.remove(t);
    }

    public List<Package> getPackages() {
        HashSet<Package> set = new HashSet<Package>();

        for (Type t: types) {
            set.add(t.getPackage());
        }

        ArrayList<Package> list = new ArrayList<Package>(set);
        Collections.sort(list);
        return list;
    }

    public <T extends  Node> T create(String name, Class<T> cls) {
        try {
            Constructor<T> ctor = cls.getConstructor(String.class, Design.class);
            T item = ctor.newInstance(name, this);

            if (item instanceof Type)
                types.add((Type)item);

            return item;
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    Reference getReference(Node from, Node to, int tag) {
        return getReference(from, to, 1, tag);
    }

    Reference getReference(Node from, Node to, int dimension, int tag) {
        if (from == null || to == null) return null;

        Reference ref = Reference.get(from, to, tag);
        ref.setDimension(dimension);
        return ref;
    }

    protected Reference getReference(Node from, String to, int tag) {
        if (from == null) return null;

        int dimension = 0;
        int arrindex = to.indexOf("[]");
        String realName = to.substring(0, arrindex < 0 ? to.length() : arrindex);
        while (arrindex >= 0)
         {
            dimension++;
            arrindex = to.indexOf("[]", arrindex + 2);
        }

        Type reft = getType(realName);
        if (reft != null)
            return getReference(from, reft, dimension, tag);
         else
         {
            Reference ref = Reference.get(from, realName, tag);
            ref.setDimension(dimension);
            return ref;
        }
    }

    public Design() {
        this(new ArrayList<String>());
    }

    Design(ArrayList<String> modlog) {
        types = new ArrayList<Type>();
        modificationLog = modlog;
        name = String.format("Design #%s", UUID.randomUUID().toString());
    }

    public Set<Action> getPossibleActions() {
        return TransitionManager.getPossibleActions(this);
    }

    public Design copy() {
        return DesignCloner.clone(this);
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
     public String toString() {
        return name;
    }

    @Override
     public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        ArrayList<Method> methods = new ArrayList<Method>();
        ArrayList<Field> fields = new ArrayList<Field>();

        in.readInt(); //version

        name
         = in.readUTF();
        tag = in.readObject();

        int modlogcnt = in.readInt();
        for (int i = 0; i < modlogcnt; i++) {
            modificationLog.add(in.readUTF());
        }

        int typecnt = in.readInt();
        for (int i = 0; i < typecnt; i++) {
            Type t = new Type(in.readUTF(), this);
            types.add(t);
            t.setFlags(in.readInt());

            int fldcnt = in.readInt();
            for (int j = 0; j < fldcnt; j++) {
                Field f = t.createField(in.readUTF());
                f.setFlags(in.readInt());

                fields.add(f);
            }

            int mthcnt = in.readInt();
            for (int j = 0; j < mthcnt; j++) {
                Method m = t.createMethod(in.readUTF());
                m.setFlags(in.readInt());

                methods.add(m);
            }
        }

        for (int i = 0; i < typecnt; i++) {
            Type t = types.get(i);

            int superindex = in.readInt();
            int parentindex = in.readInt();

            if (superindex >= 0) t.setSuperType(types.get(superindex));
            if (parentindex >= 0) t.setParentType(types.get(parentindex));

            int interfacecnt = in.readInt();
            for (int j = 0; j < interfacecnt; j++) {
                int interfaceindex = in.readInt();

                if (interfaceindex >= 0)
                    t.addInterface(types.get(interfaceindex));
            }
        }

        int fieldcnt = in.readInt();
        for (int i = 0; i < fieldcnt; i++) {
            int ftypeindex = in.readInt();
            if (ftypeindex >= 0)
                fields.get(i).setType(types.get(ftypeindex));
                //fields.get(i).setFieldType(in.readUTF());
        }

        int methodcnt = in.readInt();
        for (int i = 0; i < methodcnt; i++) {
            Method m = methods.get(i);

            int calledcnt = in.readInt();
            for (int j = 0; j < calledcnt; j++) {
                int calledindex = in.readInt();
                if (calledindex >= 0)
                    m.addCalledMethod(methods.get(calledindex));
            }

            int accessedcnt = in.readInt();
            for (int j = 0; j < accessedcnt; j++) {
                int accessedindex = in.readInt();
                if (accessedindex >= 0)
                    m.addAccessedField(fields.get(accessedindex));
            }

            int instantiatedcnt = in.readInt();
            for (int j = 0; j < instantiatedcnt; j++) {
                int instantiatedindex = in.readInt();
                if (instantiatedindex >= 0)
                    m.addInstantiatedType(types.get(instantiatedindex));
            }

            int paramscnt = in.readInt();
            for (int j = 0; j < paramscnt; j++) {
                int dim = in.readInt();
                boolean internal = in.readBoolean();

                if (internal)
                    m.addParameter(types.get(in.readInt()), dim);
                 else
                    m.addParameter(in.readUTF(), dim);
            }
        }
    }

    @Override
     public void writeExternal(ObjectOutput out) throws IOException {
        ArrayList<Method> methods = new ArrayList<Method>();
        ArrayList<Field> fields = new ArrayList<Field>();

        out.writeInt(0); //version
        out.
        writeUTF(name);
        out.writeObject(tag);

        out.writeInt(modificationLog.size());
        for (String mod: modificationLog) {
            out.writeUTF(mod);
        }

        out.writeInt(types.size());
        for (int i = 0; i < types.size(); i++) {
            Type t = types.get(i);

            out.writeUTF(t.getName());
            out.writeInt(t.getFlags());

            List<Method> tm = t.getMethods();
            List<Field> tf = t.getFields();

            out.writeInt(tf.size());
            for (int j = 0; j < tf.size(); j++) {
                out.writeUTF(tf.get(j).getName());
                //out.writeUTF(tf.get(j).getFieldType());
                out.
                writeInt(tf.get(j).getFlags());

                fields.add(tf.get(j));
            }

            out.writeInt(tm.size());
            for (int j = 0; j < tm.size(); j++) {
                out.writeUTF(tm.get(j).getName());
                out.writeInt(tm.get(j).getFlags());

                methods.add(tm.get(j));
            }
        }

        for (int i = 0; i < types.size(); i++) {
            Type t = types.get(i);

            out.writeInt(types.indexOf(t.getSuperType()));
            out.writeInt(types.indexOf(t.getParentType()));

            List<Type> interfaces = t.getInterfaces();
            out.writeInt(interfaces.size());
            for (int j = 0; j < interfaces.size(); j++)
                out.writeInt(types.indexOf(interfaces.get(j)));
        }

        out.writeInt(fields.size());
        for (int i = 0; i < fields.size(); i++) {
            out.writeInt(types.indexOf(fields.get(i).getType()));
            //out.writeUTF(fields.get(i).getFieldType());
        }

        out.writeInt(methods.size());
        for (int i = 0; i < methods.size(); i++) {
            Method m = methods.get(i);

            List<Method> called = m.getCalledMethods();
            List<Field> accessed = m.getAccessedFields();
            List<Type> instantiated = m.getInstantiatedTypes();
            List<Reference> params = m.getRawParameters();

            out.writeInt(called.size());
            for (Method cm: called)
                out.writeInt(methods.indexOf(cm));

            out.writeInt(accessed.size());
            for (Field af: accessed)
                out.writeInt(fields.indexOf(af));

            out.writeInt(instantiated.size());
            for (Type it: instantiated)
                out.writeInt(types.indexOf(it));

            out.writeInt(params.size());
            for (Reference ref: params) {
                Type target = (Type)ref.getTarget();

                out.writeInt(ref.getDimension());
                out.writeBoolean(target != null);
                if (target != null) {
                    out.writeInt(types.indexOf(target));
                } else {
                    out.writeUTF(ref.toRawString());
                }
            }
        }
    }
}
