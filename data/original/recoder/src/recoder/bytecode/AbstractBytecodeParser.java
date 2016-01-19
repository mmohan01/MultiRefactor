package recoder.bytecode;

import java.io.IOException;
import java.io.InputStream;

abstract public class AbstractBytecodeParser {
	abstract public ClassFile parseClassFile(InputStream is, String location, boolean useJava5Features) throws IOException;
	
	public ClassFile parseClassFile(InputStream is, String location) throws IOException {
		return parseClassFile(is, location, true);
	}
	
	public ClassFile parseClassFile(InputStream is) throws IOException {
		return parseClassFile(is, null);
	}

	public ClassFile parseClassFile(InputStream is, boolean useJava5Features) throws IOException {
		return parseClassFile(is, null, useJava5Features);
	}

}
