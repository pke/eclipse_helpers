package eclipseutils.ui.copyto.api;

import java.util.Collection;

public interface Results {

	Collection<Result> getSuccesses();

	Collection<Result> getFailures();

}
