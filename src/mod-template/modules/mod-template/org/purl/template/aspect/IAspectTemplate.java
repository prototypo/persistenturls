package org.purl.template.aspect;

import org.purl.template.Template;
import com.ten60.netkernel.urii.IURAspect;

public interface IAspectTemplate extends IURAspect {
	public Template getTemplate();
}
