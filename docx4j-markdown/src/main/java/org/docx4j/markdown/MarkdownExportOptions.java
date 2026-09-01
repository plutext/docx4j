package org.docx4j.markdown;

/**
 * Options for {@link MarkdownExporter}.  Per-call behavior is configured here,
 * not via Docx4jProperties globals.
 */
public class MarkdownExportOptions {

	/**
	 * What to do with tracked changes.
	 */
	public enum TrackedChangesPolicy {
		/** As if all changes were accepted: insertions kept, deletions dropped (the default). */
		ACCEPT,
		/** Insertions as plain text, deletions as ~~strikethrough~~. */
		MARKUP
	}

	private TrackedChangesPolicy trackedChangesPolicy = TrackedChangesPolicy.ACCEPT;
	private String imageDirPath;
	private String imageTargetUri;

	public TrackedChangesPolicy getTrackedChangesPolicy() {
		return trackedChangesPolicy;
	}

	public MarkdownExportOptions setTrackedChangesPolicy(TrackedChangesPolicy trackedChangesPolicy) {
		this.trackedChangesPolicy = trackedChangesPolicy;
		return this;
	}

	public String getImageDirPath() {
		return imageDirPath;
	}

	/**
	 * Directory images are extracted into (created if necessary), with the
	 * markdown linking to them relatively — cf the HTML export's
	 * imageDirPath.  When unset (the default), images become data URIs, so
	 * the markdown is self-contained and the exporter writes no files.
	 */
	public MarkdownExportOptions setImageDirPath(String imageDirPath) {
		this.imageDirPath = imageDirPath;
		return this;
	}

	public String getImageTargetUri() {
		return imageTargetUri;
	}

	/**
	 * The URI prefix used in image links when imageDirPath is set (eg
	 * "images").  Default: the file name alone.
	 */
	public MarkdownExportOptions setImageTargetUri(String imageTargetUri) {
		this.imageTargetUri = imageTargetUri;
		return this;
	}

}
