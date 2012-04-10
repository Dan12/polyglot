package polyglot.util;

import java.io.File;
import static java.io.File.separator;
import static java.io.File.separatorChar;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.tools.FileObject;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

public class CustomExtFileManager implements StandardJavaFileManager {
	
	private final StandardJavaFileManager javac_fm; // JavacFileManager used by javac
	
	/** Map for storing in-memory FileObjects and associated fully qualified names */
	private final Map<String, JavaFileObject> absPathObjMap;
	/** Map for storing fully qualified package names and contained JavaFileObjects */
	private final Map<String, Set<JavaFileObject>> pathObjectMap;

	public static final String DEFAULT_PKG = "intermediate_output";

	public CustomExtFileManager() {
		javac_fm = ToolProvider.getSystemJavaCompiler().getStandardFileManager(null, null, null);
		absPathObjMap = new HashMap<String, JavaFileObject>();
		pathObjectMap = new HashMap<String, Set<JavaFileObject>>();
	}

	public void close() throws IOException {
		javac_fm.close();
	}

	public void flush() throws IOException {
		javac_fm.flush();
	}

	public ClassLoader getClassLoader(Location location) {
		return javac_fm.getClassLoader(location);
	}
	
	public FileObject getFileForInput(Location location, String packageName, String relativeName) throws IOException {
		if (location.equals(StandardLocation.SOURCE_OUTPUT)) {
			String newName = separator + (packageName.equals("") ? ("" + relativeName) : (packageName.replace('.', separatorChar) + separator + relativeName));
			for (File f : javac_fm.getLocation(location)) {
				String absPath = f.getAbsolutePath() + newName;
				JavaFileObject fo = absPathObjMap.get(absPath);
				if (fo != null)
					return fo;
			}
			return null;
		}
		return javac_fm.getFileForInput(location, packageName, relativeName);
	}

	public FileObject getFileForOutput(Location location, String packageName, String relativeName, FileObject sibling) throws IOException {
		if (location == null || !location.equals(StandardLocation.SOURCE_OUTPUT) || !javac_fm.hasLocation(location))
			return null;
		if (packageName == null || packageName.equals(""))
			packageName = DEFAULT_PKG;
		String path = "";
		if(sibling == null) {
			for(File f : javac_fm.getLocation(location)) {
				path = f.getAbsolutePath();
				break;
			}
		} else {
			for(File f : javac_fm.getLocation(location)) {
				String[] files = f.list();
				for(String s : files)
					if(s.equals(sibling.getName()))
						path = f.getAbsolutePath();
			}
		}
		String parentFilePath = path + separator + packageName.replace('.', separatorChar);
		String absPath = parentFilePath + separator + relativeName;
		Kind k;
		if(absPath.endsWith(".java"))
			k = Kind.SOURCE;
		else if(absPath.endsWith(".class"))
			k = Kind.CLASS;
		else if(absPath.endsWith(".htm") || absPath.endsWith(".html"))
			k = Kind.HTML;
		else
			k = Kind.OTHER;
		JavaFileObject fo = new CustomJavaFileObject(absPath, k, true);
		absPathObjMap.put(absPath, fo);
		if(pathObjectMap.containsKey(parentFilePath))
			pathObjectMap.get(parentFilePath).add(fo);
		else {
			Set<JavaFileObject> s = new HashSet<JavaFileObject>();
			s.add(fo);
			pathObjectMap.put(parentFilePath, s);
		}
		return fo;
	}

	public JavaFileObject getJavaFileForInput(Location location, String className, Kind kind) throws IOException {
		if (location.equals(StandardLocation.SOURCE_OUTPUT)) {
			String clazz = separator + className.replace('.', separatorChar) + kind.extension;
			for (File f : javac_fm.getLocation(location)) {
				String absPath = f.getAbsolutePath() + clazz;
				JavaFileObject fo = absPathObjMap.get(absPath);
				if (fo != null)
					return fo;
			}
			return null;
		}
		return javac_fm.getJavaFileForInput(location, className, kind);
	}
	
	public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind, FileObject sibling) throws IOException {
		if (kind.equals(Kind.SOURCE)) {
			if(location == null || !location.equals(StandardLocation.SOURCE_OUTPUT) || !javac_fm.hasLocation(location))
				return null;
			String path = "";
			if(sibling == null) {
				for(File f : javac_fm.getLocation(location)) {
					path = f.getAbsolutePath();
					break;
				}
			} else {
				String siblingPath = sibling.getName();
				path = siblingPath.substring(0, siblingPath.lastIndexOf(separatorChar));
			}
			String classNamePath = className.replace('.', separatorChar);
			String absPath;
			if(classNamePath.startsWith(path))
				absPath = classNamePath;
			else
				absPath = path + separator + classNamePath.substring(classNamePath.lastIndexOf(separatorChar) + 1);
			JavaFileObject fo = new CustomJavaSourceObject(absPath);
			if(pathObjectMap.containsKey(path))
				pathObjectMap.get(path).add(fo);
			else {
				Set<JavaFileObject> s = new HashSet<JavaFileObject>();
				s.add(fo);
				pathObjectMap.put(path, s);
			}
			return fo;
		} else if (kind.equals(Kind.CLASS)) {
			if(location == null || !location.equals(StandardLocation.CLASS_OUTPUT) || !javac_fm.hasLocation(location))
				return null;
			String path = "";
			if(sibling == null) {
				for(File f : javac_fm.getLocation(location)) {
					path = f.getAbsolutePath();
					break;
				}
			} else {
				String siblingPath = sibling.getName();
				path = siblingPath.substring(0, siblingPath.lastIndexOf(separatorChar));
			}
			int lastDot = className.lastIndexOf('.');
			if(lastDot > 0) {
				String pkg = className.substring(0, lastDot);
				File f = new File(path + separator + pkg.replace('.', separatorChar));
				if(!f.exists())
					f.mkdirs();
			}
			String absPath = path + separator + className.replace('.', separatorChar);
			return new CustomJavaClassObject(absPath);
		} else {
			throw new UnsupportedOperationException();
		}
	}

	public boolean handleOption(String current, Iterator<String> remaining) {
		return javac_fm.handleOption(current, remaining);
	}

	public boolean hasLocation(Location location) {
		return javac_fm.hasLocation(location);
	}

	public String inferBinaryName(Location location, JavaFileObject file) {
		if (file instanceof CustomFileObject) {
			String className = ((CustomFileObject) file).getName();
			return className.substring(className.lastIndexOf('.') + 1);
		}
		if (file instanceof CustomJavaSourceObject) {
			String className = ((CustomJavaSourceObject) file).getName();
			return className.substring(className.lastIndexOf('.') + 1);
		}
		if (file instanceof CustomJavaClassObject) {
			String className = ((CustomJavaClassObject) file).getName();
			return className.substring(className.lastIndexOf('.') + 1);
		}
		return javac_fm.inferBinaryName(location, file);
	}

	private void setFiller(String parentFilePath, Set<Kind> kinds, Set<JavaFileObject> s) {
		for(JavaFileObject fo : pathObjectMap.get(parentFilePath)) {
			if (kinds.contains(Kind.SOURCE) && fo.getKind().equals(Kind.SOURCE))
				s.add(fo);
			else if (kinds.contains(Kind.CLASS) && fo.getKind().equals(Kind.CLASS))
				s.add(fo);
			else if (kinds.contains(Kind.HTML) && fo.getKind().equals(Kind.HTML))
				s.add(fo);
			else if (kinds.contains(Kind.OTHER))
				s.add(fo);
		}
	}
	
	public Iterable<JavaFileObject> list(Location location, String packageName, Set<Kind> kinds, boolean recurse) throws IOException {
		if(location == null || !javac_fm.hasLocation(location))
			return new HashSet<JavaFileObject>();
		if(location.equals(StandardLocation.SOURCE_OUTPUT)) {
			Set<JavaFileObject> s = new HashSet<JavaFileObject>();
			String pkg = separator + packageName.replace('.', separatorChar);
			for (File file : javac_fm.getLocation(location)) {
				String parentFilePath = file.getAbsolutePath() + pkg;
				if(pathObjectMap.containsKey(parentFilePath)) {
					setFiller(parentFilePath, kinds, s);
					if(recurse)
						for(String str : pathObjectMap.keySet())
							if(str.startsWith(parentFilePath))
								setFiller(str, kinds, s);
				}
			}
			return s;
		}
		return javac_fm.list(location, packageName, kinds, recurse);
	}

	public int isSupportedOption(String option) {
		return javac_fm.isSupportedOption(option);
	}

	public Iterable<? extends JavaFileObject> getJavaFileObjects(File... files) {
		throw new UnsupportedOperationException();
	}

	public Iterable<? extends JavaFileObject> getJavaFileObjects(
			String... names) {
		throw new UnsupportedOperationException();
	}

	public Iterable<? extends JavaFileObject> getJavaFileObjectsFromFiles(
			Iterable<? extends File> files) {
		throw new UnsupportedOperationException();
	}

	public Iterable<? extends JavaFileObject> getJavaFileObjectsFromStrings(
			Iterable<String> names) {
		throw new UnsupportedOperationException();
	}

	public Iterable<? extends File> getLocation(Location location) {
		return javac_fm.getLocation(location);
	}

	public boolean isSameFile(FileObject a, FileObject b) {
		return a.toUri().equals(b.toUri());
	}

	public void setLocation(Location location, Iterable<? extends File> path) throws IOException {
		javac_fm.setLocation(location, path);
	}

}