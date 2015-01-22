# Apply the templates

define paaspuppet::push_templates ($target) {
  file { "${target}/${name}":
    ensure  => present,
    owner   => $owner,
    group   => $group,
    mode    => '0755',
    content => template("paaspuppet/${name}.erb"),
  }
}
