package org.docx4j.events;


public class StartEvent extends Docx4jEvent {

	
	/**
	 * Use this to signal the start of a Job.
	 * 
	 * @param job
	 * @param eventType
	 */
	public StartEvent(JobIdentifier job) {
		super( job);
	}

	/**
	 * Use this to signal the start of work on a specific pkg in a job.
	 * 
	 * @param job
	 * @param eventType
	 */
	public StartEvent(JobIdentifier job,  PackageIdentifier pkgIdentifier) {
		super( job,   pkgIdentifier);
	}
	
	/**
	 * Use this to signal the start of work on a specific pkg,
	 * where you didn't define an overall job.
	 * 
	 * @param job
	 * @param eventType
	 */
	public StartEvent(PackageIdentifier pkgIdentifier) { 
		super(  pkgIdentifier);
	}
	
	/**
	 * Use this to signal the start of a process step,
	 * where you didn't define an overall job.
	 * 
	 * @param job
	 * @param eventType
	 */
	public StartEvent(PackageIdentifier pkgIdentifier, ProcessStep processStep) {
		super( pkgIdentifier,  processStep);
	}
	
	/**
	 * Use this to signal the start of a process step,
	 * on some pkg in some overall job.
	 * 
	 * @param job
	 * @param eventType
	 */
	public StartEvent(JobIdentifier job,  PackageIdentifier pkgIdentifier, ProcessStep processStep) {
		
		super( job,   pkgIdentifier,  processStep);
	}	
}
