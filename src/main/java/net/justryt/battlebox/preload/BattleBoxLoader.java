package net.justryt.battlebox.preload;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;

public class BattleBoxLoader implements PluginLoader {
    @Override
    public void classloader(final PluginClasspathBuilder classpathBuilder) {
        final var resolver = new MavenLibraryResolver();

        resolver.addDependency(new Dependency(new DefaultArtifact("com.zaxxer:HikariCP:7.1.0"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("com.mysql:mysql-connector-j:26.7.0"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("org.mariadb.jdbc:mariadb-java-client:3.5.10"), null));

        classpathBuilder.addLibrary(resolver);
    }
}
