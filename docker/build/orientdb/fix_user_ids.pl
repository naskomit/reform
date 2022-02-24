#!/usr/bin/env perl
use strict;
use warnings;

# script to change the user and group id of the docker user so that it matches
# the ids of the user running the docker to help sharing files between the host
# and the guest

my ($USER, $HOST_UID, $HOST_GID, $GROUP) = @ARGV;

$GROUP ||= 'qbstaff';

sub usage {
  my $msg = shift || "";
  $msg .= "\n" if $msg;
  my $usage =
  qq{USAGE: $0 username uid gid [groupname]
  };

  die($msg . $usage . "\n");
}

# a stderred friendly log function
sub logme {
  print STDERR "@_\n";
}

usage("Give gid") unless($HOST_GID);

my $uid = getpwnam $USER;
#my $gid = getgrnam $USER;
my ($user, $passwd, $uid2, $gid ) = getpwuid $uid;

if ($uid ne $HOST_UID || $gid ne $HOST_GID) {
  logme("fixing USER $USER uid from $uid to $HOST_UID");

  # we may need to create the group
  unless (getgrgid($HOST_GID)) {
    # poor man's way to make the group name unique if if exists
    my $i = 1;
    my $group = $GROUP;
    $group = "$GROUP" . $i++ while(getgrnam($group));

    logme("creating group $group with id $HOST_GID");
    my $cmd = qq{addgroup --gid $HOST_GID $group};
    system($cmd) and die "unable to execute command '$cmd': $!";
  }

  my $cmd = qq{usermod -u $HOST_UID -g $HOST_GID $USER};
  warn "fixing user ids using cmd='$cmd'\n";
  system($cmd) and die "unable to execute command '$cmd': $!";
} else {
    logme("nothing to fix");
}


 
