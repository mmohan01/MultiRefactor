/**
 * 
 */
package recoder.service;

import java.util.Collections;
import java.util.List;

import recoder.ModelException;

/**
 * @author Tobias
 *
 */
public class TooManyErrorsException extends ModelException {
	private final List<Exception> errors;
	
	public TooManyErrorsException(String s, List<Exception> errors) {
		super(s);
		this.errors = Collections.unmodifiableList(errors);
	}
	
	public List<Exception> getErrors() {
		return errors;
	}
}
