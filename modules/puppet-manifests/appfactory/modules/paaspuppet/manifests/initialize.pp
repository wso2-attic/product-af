define paaspuppet::initialize (
  $repo,
  $local_dir,
  $mode,
  $owner,
  $deployment_files
) {

  exec {
    "creating_local_package_repo_for_${name}":
    path      => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/java/bin/',
    unless    => "test -d ${local_dir}",
    command   => "mkdir -p ${local_dir}";

  }

  downloadPuppetFile{$deployment_files:
    repo      => $repo,
    local_dir => $local_dir,
    require   => Exec["creating_local_package_repo_for_${name}"];
  }
}

define downloadPuppetFile(
  $repo,
  $local_dir
){
    exec{"${name}":
      path      => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin',
      cwd       => $local_dir,
      unless    => "test -f ${local_dir}/${name}",
      command   => "wget -q ${repo}/${name}",
      logoutput => 'on_failure',
      creates   => "${local_dir}/${name}",
      timeout   => 0
  }
}
