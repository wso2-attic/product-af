package org.wso2.carbon.appfactory.issuetracking.beans;
/**
 * 
 * Basic suammry of issues for application version
 *
 */
public class IssueSummary {

	private String appKey;
	private String version;

	private int bugCount;
	private IssueSummaryInStatus issueSummaryInStatusForBug = new IssueSummaryInStatus();

	private int featureCount;
	private IssueSummaryInStatus issueSummaryInStatusForFeature = new IssueSummaryInStatus();

	private int supportCount;
	private IssueSummaryInStatus issueSummaryInStatusForSupport = new IssueSummaryInStatus();

	public IssueSummaryInStatus getIssueSummaryInStatusForBug() {
		return issueSummaryInStatusForBug;
	}

	public void setIssueSummaryInStatusForBug(
			IssueSummaryInStatus issueSummaryInStatusForBug) {
		this.issueSummaryInStatusForBug = issueSummaryInStatusForBug;
	}

	public IssueSummaryInStatus getIssueSummaryInStatusForFeature() {
		return issueSummaryInStatusForFeature;
	}

	public void setIssueSummaryInStatusForFeature(
			IssueSummaryInStatus issueSummaryInStatusForFeature) {
		this.issueSummaryInStatusForFeature = issueSummaryInStatusForFeature;
	}

	public IssueSummaryInStatus getIssueSummaryInStatusForSupport() {
		return issueSummaryInStatusForSupport;
	}

	public void setIssueSummaryInStatusForSupport(
			IssueSummaryInStatus issueSummaryInStatusForSupport) {
		this.issueSummaryInStatusForSupport = issueSummaryInStatusForSupport;
	}

	public void increaseCount(String type, String statusId) {
		if (type.equals("Bug")) {
			this.bugCount++;
			increaseCountByStatus(statusId, issueSummaryInStatusForBug);
		} else if (type.equals("Feature")) {
			this.featureCount++;
			increaseCountByStatus(statusId, issueSummaryInStatusForFeature);

		} else if (type.equals("Support")) {
			this.supportCount++;
			increaseCountByStatus(statusId, issueSummaryInStatusForSupport);

		}
	}

	private void increaseCountByStatus(String statusId,
			IssueSummaryInStatus issueSummaryInStatus) {
		if (statusId.equals("New")) {
			issueSummaryInStatus.setOpenCount(issueSummaryInStatus
					.getOpenCount() + 1);
		} else if (statusId.equals("In Progress")) {
			issueSummaryInStatus.setInProgressCount(issueSummaryInStatus
					.getInProgressCount() + 1);
		} else if (statusId.equals("Resolved")) {
			issueSummaryInStatus.setResolvedCount(issueSummaryInStatus
					.getResolvedCount() + 1);
		} else if (statusId.equals("Feedback")) {
			issueSummaryInStatus.setFeedbackCount(issueSummaryInStatus
					.getFeedbackCount() + 1);
		} else if (statusId.equals("Closed")) {
			issueSummaryInStatus.setClosedCount(issueSummaryInStatus
					.getClosedCount() + 1);
		} else if (statusId.equals("Rejected")) {
			issueSummaryInStatus.setRejectedCount(issueSummaryInStatus
					.getRejectedCount() + 1);
		}
	}

	public String getAppKey() {
		return appKey;
	}

	public void setAppKey(String appKey) {
		this.appKey = appKey;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public int getBugCount() {
		return bugCount;
	}

	public void setBugCount(int bugCount) {
		this.bugCount = bugCount;
	}

	public int getFeatureCount() {
		return featureCount;
	}

	public void setFeatureCount(int featureCount) {
		this.featureCount = featureCount;
	}

	public int getSupportCount() {
		return supportCount;
	}

	public void setSupportCount(int supportCount) {
		this.supportCount = supportCount;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null || obj.getClass() != this.getClass()) {
			return false;
		}
		IssueSummary appBeanObject = (IssueSummary) obj;

		return this.getAppKey() != null
				&& this.getAppKey().equals(appBeanObject.getAppKey())
				&& this.getVersion() != null
				&& this.getVersion().equals(appBeanObject.getVersion());

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((this.appKey == null) ? 0 : this.appKey.hashCode());
		result = prime * result
				+ ((this.version == null) ? 0 : this.version.hashCode());

		return result;
	}

	public class IssueSummaryInStatus {
		private int openCount;
		private int inProgressCount;
		private int resolvedCount;
		private int feedbackCount;
		private int closedCount;
		private int rejectedCount;

		public int getOpenCount() {
			return openCount;
		}

		public void setOpenCount(int openCount) {
			this.openCount = openCount;
		}

		public int getInProgressCount() {
			return inProgressCount;
		}

		public void setInProgressCount(int inProgressCount) {
			this.inProgressCount = inProgressCount;
		}

		public int getResolvedCount() {
			return resolvedCount;
		}

		public void setResolvedCount(int resolvedCount) {
			this.resolvedCount = resolvedCount;
		}

		public int getFeedbackCount() {
			return feedbackCount;
		}

		public void setFeedbackCount(int feedbackCount) {
			this.feedbackCount = feedbackCount;
		}

		public int getClosedCount() {
			return closedCount;
		}

		public void setClosedCount(int closedCount) {
			this.closedCount = closedCount;
		}

		public int getRejectedCount() {
			return rejectedCount;
		}

		public void setRejectedCount(int rejectedCount) {
			this.rejectedCount = rejectedCount;
		}

	}

}
