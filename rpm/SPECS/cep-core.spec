# Copyright 2015 Telefonica Investigacion y Desarrollo, S.A.U
#
# This file is part of perseo-core.
#
# perseo-core is free software: you can redistribute it and/or
# modify it under the terms of the GNU General Public License as
# published by the Free Software Foundation, either version 2 of the
# License, or (at your option) any later version.
#
# perseo-core is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
# General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with perseo-core. If not, see http://www.gnu.org/licenses/.
#
# For those usages not covered by this license please contact with
# iot_support at tid dot es

Summary: Perseo Complex Event Processing Core
Name: perseo-cep-core
Version: %{_product_version}
Release: %{_product_release}
License: AGPLv3
BuildRoot: %{_topdir}/BUILDROOT/
BuildArch: x86_64
Requires: tomcat, logrotate
Group: Applications/Engineering
Vendor: Telefonica I+D

%description
Perseo is the Complex Event Process for the Fiware platform.

# System folders
%define _install_dir /usr/share/tomcat/webapps
%define _perseoCepCore_log_dir /var/log/perseo

# RPM Building folder
%define _build_root_project %{buildroot}%{_install_dir}
# -------------------------------------------------------------------------------------------- #
# prep section, setup macro:
# -------------------------------------------------------------------------------------------- #
%prep
echo "[INFO] Preparing installation"
# Create rpm/BUILDROOT folder
rm -Rf $RPM_BUILD_ROOT && mkdir -p $RPM_BUILD_ROOT
[ -d %{_build_root_project} ] || mkdir -p %{_build_root_project}

#FIXME: this line should be adjusted by the release.sh (not yet in the repo). Once the release.sh is ok with this, please remove this fixme line
cp -ax %{_topdir}/../target/fiware-perseo-core-0.1.1-SNAPSHOT.war %{_build_root_project}/perseo-core.war

cp -R %{_topdir}/SOURCES/etc %{buildroot}

# -------------------------------------------------------------------------------------------- #
# post-install section:
# -------------------------------------------------------------------------------------------- #
%post

echo "[INFO] Configuring application"

    echo "[INFO] Creating log directory"
    mkdir -p %{_perseoCepCore_log_dir}
    chown -R %{_project_user}:%{_project_user} %{_perseoCepCore_log_dir}
    chown -R %{_project_user}:%{_project_user} _install_dir
    chmod g+s %{_perseoCepCore_log_dir}
    setfacl -d -m g::rwx %{_perseoCepCore_log_dir}
    setfacl -d -m o::rx %{_perseoCepCore_log_dir}

echo "Done"

# -------------------------------------------------------------------------------------------- #
# pre-uninstall section:
# -------------------------------------------------------------------------------------------- #
%preun

if [ $1 == 0 ]; then

  echo "[INFO] Removing application log files"
  # Log
  [ -d %{_perseoCepCore_log_dir} ] && rm -rfv %{_perseoCepCore_log_dir}

  echo "[INFO] Removing application files"
  # Installed files
  rm -rfv %{_install_dir}/perseo-core*

fi

%postun
%clean
rm -rf $RPM_BUILD_ROOT

# -------------------------------------------------------------------------------------------- #
# Files to add to the RPM
# -------------------------------------------------------------------------------------------- #
%files
%defattr(755,%{_project_user},%{_project_user},755)
%config /etc/sysconfig/logrotate-perseo-core-size
%config /etc/logrotate.d/logrotate-perseo-core.conf
%config /etc/cron.d/cron-logrotate-perseo-core-size
%{_install_dir}/perseo-core.war

%changelog
* Mon Jan 19 2015 Carlos Romero Brox <brox@tid.es> 0.1.1
- Fix: change log format. Add time level. Remove class and line number from message

* Fri Nov 7 2014 Carlos Romero Brox <brox@tid.es> 0.1.0
- Initial version
