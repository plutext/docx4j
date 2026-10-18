package org.docx4j.markdown;

import org.docx4j.model.text.TextOptions;
import org.docx4j.model.text.TextView;

/**
 * Options for {@link MarkdownExporter}.  Per-call behavior is configured here,
 * not via Docx4jProperties globals.
 *
 * <p>Since 17.3.4 (CR-046 phase 3) what text is written is the text model's, under its
 * {@link TextOptions} ({@link #getTextOptions()}): the view, the stories, hidden text, field
 * codes, metadata as YAML front matter, and so on, under the names of the WD's section 10.  The
 * options here are how it is written.</p>
 */
public class MarkdownExportOptions {

	/**
	 * What to do with tracked changes.
	 *
	 * @deprecated since 17.3.4: the text model's view ({@link #setView(TextView)}), of which this
	 *     is an alias: {@code ACCEPT} is {@link TextView#ACCEPTED}, {@code MARKUP} is
	 *     {@link TextView#MARKUP}, and {@link TextView#ORIGINAL} has no policy of its own.
	 */
	@Deprecated
	public enum TrackedChangesPolicy {
		/** As if all changes were accepted: insertions kept, deletions dropped (the default). */
		ACCEPT,
		/** Since 17.3.4 the {@code markup} view: insertions {@code {++ ++}}, deletions {@code {-- --}} (CriticMarkup, WD REQ-703); before, deletions as ~~strikethrough~~. */
		MARKUP
	}

	private TextOptions textOptions = new TextOptions();
	private String imageDirPath;
	private String imageTargetUri;

	/**
	 * The text model's options the export reads with (CR-046): the view, the stories and content
	 * classes, metadata.  Never null; changed in place, or replaced by
	 * {@link #setTextOptions(TextOptions)}.  The default is the text model's: the {@code accepted}
	 * view, the main story with its text boxes and notes, hidden text left out.
	 *
	 * @since 17.3.4
	 */
	public TextOptions getTextOptions() {
		return textOptions;
	}

	/** @since 17.3.4 */
	public MarkdownExportOptions setTextOptions(TextOptions textOptions) {
		this.textOptions = textOptions == null ? new TextOptions() : textOptions;
		return this;
	}

	/** The revision view: {@code accepted} (the default), {@code original} or {@code markup}.  @since 17.3.4 */
	public TextView getView() {
		return textOptions.getView();
	}

	/** @since 17.3.4 */
	public MarkdownExportOptions setView(TextView view) {
		textOptions.view(view);
		return this;
	}

	/**
	 * @deprecated since 17.3.4: {@link #getView()}; {@code ORIGINAL} answers {@code ACCEPT}, the
	 *     policy having no value for it.
	 */
	@Deprecated
	public TrackedChangesPolicy getTrackedChangesPolicy() {
		return getView() == TextView.MARKUP ? TrackedChangesPolicy.MARKUP : TrackedChangesPolicy.ACCEPT;
	}

	/** @deprecated since 17.3.4: {@link #setView(TextView)}, which this sets. */
	@Deprecated
	public MarkdownExportOptions setTrackedChangesPolicy(TrackedChangesPolicy trackedChangesPolicy) {
		return setView(trackedChangesPolicy == TrackedChangesPolicy.MARKUP ? TextView.MARKUP : TextView.ACCEPTED);
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
