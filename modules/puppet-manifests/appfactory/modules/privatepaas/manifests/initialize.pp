define privatepaas::initialize (
  $repo,
  $local_dir,
  $target,
  $mode,
  $owner,
  $deployment_files
) {

  exec {
    "creating_target_for_${name}":
    path      => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin',
    command   => "mkdir -p ${target}";

    "creating_local_package_repo_for_${name}":
    path      => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/java/bin/',
    unless    => "test -d ${local_dir}",
    command   => "mkdir -p ${local_dir}";

  }

  downloadFile{$deployment_files:
    repo      => $repo,
    local_dir => $local_dir,
    target    => $target,
    require   => Exec["creating_local_package_repo_for_${name}",
    "creating_target_for_${name}"];
  }
}

define downloadFile(
  $repo,
  $local_dir,
  $target
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
