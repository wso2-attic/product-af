# Apply the templates

define bam::push_templates ($directory, $target) {
  file { "${target}/repository/${name}":
    ensure  => present,
    owner   => $bam::owner,
    group   => $bam::group,
    mode    => '0755',
    content => template("${directory}/${name}.erb"),
  }
}
