package org.docx4j.events;


public class EventFinished extends Docx4jEvent {

	private StartEvent started;
	public StartEvent getStartEvent() {
		return started;
	}
	
	public EventFinished(StartEvent started) {
		super( started.getJob(),   started.getPkgIdentifier(),  started.getProcessStep());
		this.started = started;
	}
	
	/**
	 * Use this to signal the end of a Job.
	 * 
	 * @param job
	 * @param eventType
	 */
	public EventFinished(JobIdentifier job) {
		super( job);
	}

	/**
	 * Use this to signal the end of work on a specific pkg in a job.
	 * 
	 * @param job
	 * @param eventType
	 */
	public EventFinished(JobIdentifier job,  PackageIdentifier pkgIdentifier) {
		super( job,   pkgIdentifier);
	}
	
	/**
	 * Use this to signal the end of work on a specific pkg,
	 * where you didn't define an overall job.
	 * 
	 * @param job
	 * @param eventType
	 */
	public EventFinished(PackageIdentifier pkgIdentifier) { 
		super(  pkgIdentifier);
	}
	
	/**
	 * Use this to signal the end of a process step,
	 * where you didn't define an overall job.
	 * 
	 * @param job
	 * @param eventType
	 */
	public EventFinished(PackageIdentifier pkgIdentifier, ProcessStep processStep) {
		super( pkgIdentifier,  processStep);
	}
	
	/**
	 * Use this to signal the end of a process step,
	 * on some pkg in some overall job.
	 * 
	 * @param job
	 * @param eventType
	 */
	public EventFinished(JobIdentifier job,  PackageIdentifier pkgIdentifier, ProcessStep processStep) {
		
		super( job,   pkgIdentifier,  processStep);
	}		
	
	// Should the result of a job be available???
	
//	private Object result;
//	public Object getResult() {
//		return result;
//	}
	
}
