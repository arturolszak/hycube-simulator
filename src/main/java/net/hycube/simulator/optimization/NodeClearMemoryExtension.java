package net.hycube.simulator.optimization;

import net.hycube.common.EntryPoint;
import net.hycube.core.InitializationException;
import net.hycube.core.NodeAccessor;
import net.hycube.environment.NodeProperties;
import net.hycube.extensions.Extension;
import net.hycube.maintenance.HyCubeNotifyProcessor;
import net.hycube.maintenance.HyCubeRecoveryExtension;
import net.hycube.messaging.processing.HyCubeReceivedMessageProcessor;
import net.hycube.messaging.processing.ReceivedMessageProcessor;

public class NodeClearMemoryExtension implements Extension, EntryPoint {

	public static final String RECOVERY_EXTENSION_KEY = "RecoveryExtension";
	
	NodeAccessor nodeAccessor;
	
	@Override
	public void initialize(NodeAccessor nodeAccessor, NodeProperties properties) throws InitializationException {
		this.nodeAccessor = nodeAccessor;
	}

	@Override
	public void postInitialize() throws InitializationException {
		
	}

	@Override
	public EntryPoint getExtensionEntryPoint() {
		return this;
	}

	@Override
	public void discard() {
		
	}

	@Override
	public Object call() {
		
		if (nodeAccessor.getNotifyProcessor() instanceof HyCubeNotifyProcessor) {
			((HyCubeNotifyProcessor)nodeAccessor.getNotifyProcessor()).clearRecentlyProcessedNodes();
		}
		
		Extension recoveryExtension = nodeAccessor.getExtension(RECOVERY_EXTENSION_KEY);
		if (recoveryExtension != null && recoveryExtension instanceof HyCubeRecoveryExtension) {
			((HyCubeRecoveryExtension)recoveryExtension).getRecoveryManager().clearNotifyNodes();
		}
		
		for (ReceivedMessageProcessor proc: nodeAccessor.getReceivedMessageProcessors()) {
			if (proc instanceof HyCubeReceivedMessageProcessor) {
				((HyCubeReceivedMessageProcessor)proc).purgeRecentMessagesForMessagesProcessedMaxRateForAllMessageTypes(nodeAccessor.getEnvironment().getTimeProvider().getCurrentTime());
			}
		}

		
		return null;
		
	}

	@Override
	public Object call(Object arg) {
		return call();
	}

	@Override
	public Object call(Object[] args) {
		return call();
	}

	@Override
	public Object call(Object entryPoint, Object[] args) {
		return call();
	}

}
