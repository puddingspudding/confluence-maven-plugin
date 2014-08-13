package confluencemavenplugin;

import java.io.*;
import java.util.*;

import org.apache.commons.io.*;
import org.apache.commons.io.filefilter.*;
import org.apache.maven.project.MavenProject;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;


/**
 * Represents the {@code confluence-maven-plugin} main class (but not the {@code Mojo}s).
 * 
 * <p>
 * All the things done by this plugin starts here. Instead, if what you need is to take a look to
 * {@code Mojo}s provided, you can see them under package {@code confluencemavenplugin.mojo}. 
 * </p>
 */
public class ConfluenceMavenPlugin {

	private static final String README_HTML = "README.html";

	public void generate(File file, MavenProject project, File outputDirectory) throws FileNotFoundException, IOException {
		if (! outputDirectory.exists())
			outputDirectory.mkdirs();
		
		Markdown markdown = new Markdown(file);
		String html = markdown.toHtml();
		html = replaceProjectProperties(html, project);
		
		String htmlFilename = FilenameUtils.getBaseName(file.getName()) + ".html";
		File outputFile = new File(outputDirectory, htmlFilename);
		
		PrintWriter writer = new PrintWriter(outputFile);
		writer.write(html);
		writer.close();
	}

	public void generateAll(File directory, MavenProject project, File outputDirectory) throws FileNotFoundException, IOException {
		Collection<File> files = FileUtils.listFiles(directory, new SuffixFileFilter(".md"), FalseFileFilter.INSTANCE);
		for (File file : files)
			generate(file, project, outputDirectory);
	}
	
	public void deploy(Confluence confluence, File outputDirectory, String parentTitle) throws DeployException {
		File readme = new File(outputDirectory, README_HTML);
		String wikiParent;
		try {
			wikiParent = confluence.addOrUpdatePage(parentTitle, readme);
			confluence.sync(
					findWikiFiles(outputDirectory), 
					wikiParent
			);
		} catch (IOException e) {
			throw new DeployException("Unable to deploy to confluence", e);
		}
	}

	private File[] findWikiFiles(File outputDirectory) {
		Collection<File> files = FileUtils.listFiles(
				outputDirectory, 
				new AndFileFilter(
						new SuffixFileFilter(".html"),
						new NotFileFilter(new NameFileFilter(README_HTML))
				),
				FalseFileFilter.INSTANCE
		);
		return files.toArray(new File[0]);
	}

	private String replaceProjectProperties(String html, MavenProject project) {
		StringWriter writerOnText = new StringWriter();
		boolean evaluated = Velocity.evaluate(
				new VelocityContext(Collections.singletonMap("project", project)), 
				writerOnText, 
				getClass().getName(), 
				html
				);
		if (! evaluated)
			throw new RuntimeException("Unable to replace project properties inside content '" + html + "'");
		
		return writerOnText.toString();
	}

	protected String publish(Confluence confluence, File file, String parentTitle) throws DeployException {
		if (! confluence.existPage(parentTitle))
			throw new DeployException("Unable to find any page with title '" + parentTitle + "' to use as parent");

		try {
			return confluence.addOrUpdatePage(parentTitle, file);
		} catch (IOException e) {
			throw new DeployException("Unable to deploy a page '" + file + "'", e);
		}
	}

}
