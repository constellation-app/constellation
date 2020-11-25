"""
Compile GLSL shaders in a directory to SPIR-V bytecode.

Copyright 2010-2020 Australian Signals Directorate
"""

import os
import subprocess
import hashlib
from os import walk, environ
from sys import platform, stdout, stderr


def compile_shader(path_to_glslc, shader_path, stage, out_name, md5_name):
    """

    -fauto-map-locations SPIR-V and later GLSL versions require inputs and outputs to be bound to an attribute location, this just assigns them automagically
    -fauto-bind-uniforms SPIR-V and later GLSL versions require uniforms to have explicit binding, this just assigns them automagically
    -O optimises performance over size
    -o is the output file

    :param path_to_glslc:
    :param shader_path:
    :param out_name:
    :return:
    """
    cmd_line = '{0} --target-spv=spv1.0 -fauto-map-locations -fauto-bind-uniforms -O -o {1} -fshader-stage={2} {3}'\
        .format(path_to_glslc,
                out_name,
                stage,
                shader_path)
    result = subprocess.call(cmd_line,
                             stdin=None,
                             stdout=stdout,
                             stderr=stderr)

    # Write MD5 of source
    md5_file = open(md5_name, 'wb')

    hash_md5 = hashlib.md5()
    with open(shader_path, "rb") as f:
        for chunk in iter(lambda: f.read(4096), b""):
            hash_md5.update(chunk)
    md5_file.write(hash_md5.digest())
    md5_file.close()

    if result == 0:
        print('Compiled {0}, MD5: {1}'.format(shader_path, hash_md5.hexdigest()))
    else:
        print(result)


def compile_shaders(src_dir, dst_dir):
    if not os.path.exists(dst_dir):
        os.makedirs(dst_dir)

    # Check the GLSL compiler is available
    if platform == 'win32':
        VK_SDK_PATH = environ.get('VK_SDK_PATH')
        if VK_SDK_PATH and os.path.exists(VK_SDK_PATH):
            path_to_glslc = os.path.join(VK_SDK_PATH, 'bin', 'glslc.exe')
            if os.path.exists(path_to_glslc):
                for (dir_path, dir_names, file_names) in walk(src_dir):
                    for file_name in file_names:
                        # exclude this script
                        if not __file__.endswith(file_name):
                            if file_name.endswith('.fs') or file_name.endswith('.frag'):
                                stage = 'frag'
                            elif file_name.endswith('.gs') or file_name.endswith('.geom'):
                                stage = 'geom'
                            elif file_name.endswith('.vs') or file_name.endswith('.vert'):
                                stage = 'vert'
                            else:
                                print('WARNING: {0} skipped, supported file extensions: [.gs, .fs, .vs]'.format(file_name))
                                continue

                            in_name = os.path.join(dir_path, file_name)
                            out_name = os.path.join(dst_dir, file_name) + '.spv'
                            md5_name = os.path.join(dst_dir, file_name) + '.md5'
                            compile_shader(path_to_glslc, in_name, stage, out_name, md5_name)
                    # Don't recurse
                    break; 
            else:
                raise Exception('{0} not found.'.format(path_to_glslc))
        else:
            raise Exception('VK_SDK_PATH not found.  Vulkan SDK needed to compile shaders.')

    else:
        raise NotImplementedError('Not implemented on this OS.  It should be simple to add.  Add the Vulkan SDK path and you should be gtg.')


if __name__ == '__main__':
    compile_shaders('.', 'compiled')