define privatepaas::push_templates ( $directory, $target ) {
  file { "${target}/${name}":
    ensure  => present,
    owner   => $owner,
    group   => $group,
    mode    => '0755',
    content => template("${directory}/${name}.erb"),
  }
}
