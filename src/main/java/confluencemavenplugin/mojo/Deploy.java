package confluencemavenplugin.mojo;

import org.apache.maven.plugin.*;

/**
 * Deploy documentation to a confluence space.
 * 
 * @goal deploy
 * @phase process-resources
 * 
 * @execute goal="generate"
 */
public class Deploy extends AbstractMojo {

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("[FAKE] deploying to confluence...");
	}

}